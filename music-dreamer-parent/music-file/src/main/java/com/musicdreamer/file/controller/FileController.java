package com.musicdreamer.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdreamer.common.exception.GlobalException;
import com.musicdreamer.common.result.R;
import com.musicdreamer.common.result.ResultCodeEnum;
import com.musicdreamer.file.entity.FileMetadata;
import com.musicdreamer.file.enums.FileBizTypeEnum;
import com.musicdreamer.file.service.FileService;
import com.musicdreamer.file.vo.FileUploadVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文件管理控制器
 *
 * <p>所有接口都挂在 /file 下面，网关已经配置了 /file/** → music-file 的路由。
 *
 * <p>接口一览：
 * <pre>
 * POST   /file/upload?bizType=image      上传（通用，bizType 可填 image/song/lyric/file）
 * POST   /file/upload/image              上传图片
 * POST   /file/upload/song               上传歌曲
 * POST   /file/upload/lyric              上传歌词
 * POST   /file/upload/file               上传普通文件
 * GET    /file/download/{id}             下载文件（弹出下载框）
 * GET    /file/preview/{id}              在线预览 / 在线播放
 * GET    /file/info/{id}                 查询文件信息
 * GET    /file/page                      分页查询文件列表
 * DELETE /file/delete/{id}               删除文件（默认逻辑删除）
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // ==================== 上传 ====================

    /**
     * 通用上传接口
     *
     * @param file       前端表单里的文件字段，参数名必须是 file
     * @param bizType    业务类型：image / song / lyric / file，不填默认 file
     * @param uploaderId 上传者 id（直连服务调试时用；走网关时可不传）
     */
    @PostMapping("/upload")
    public R<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "bizType", defaultValue = "file") String bizType,
                                  @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                                  HttpServletRequest request) {
        return R.ok(fileService.upload(file, bizType, resolveUploaderId(uploaderId, request)), "上传成功");
    }

    /**
     * 上传图片（歌手头像、歌单封面等）
     */
    @PostMapping("/upload/image")
    public R<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                                       HttpServletRequest request) {
        return R.ok(fileService.upload(file, FileBizTypeEnum.IMAGE.getCode(),
                resolveUploaderId(uploaderId, request)), "上传成功");
    }

    /**
     * 上传歌曲（mp3 / flac 等音频）
     */
    @PostMapping("/upload/song")
    public R<FileUploadVO> uploadSong(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                                      HttpServletRequest request) {
        return R.ok(fileService.upload(file, FileBizTypeEnum.SONG.getCode(),
                resolveUploaderId(uploaderId, request)), "上传成功");
    }

    /**
     * 上传歌词（.lrc / .txt）
     */
    @PostMapping("/upload/lyric")
    public R<FileUploadVO> uploadLyric(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                                       HttpServletRequest request) {
        return R.ok(fileService.upload(file, FileBizTypeEnum.LYRIC.getCode(),
                resolveUploaderId(uploaderId, request)), "上传成功");
    }

    /**
     * 上传普通文件
     */
    @PostMapping("/upload/file")
    public R<FileUploadVO> uploadCommonFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                                            HttpServletRequest request) {
        return R.ok(fileService.upload(file, FileBizTypeEnum.FILE.getCode(),
                resolveUploaderId(uploaderId, request)), "上传成功");
    }

    // ==================== 下载 ====================

    /**
     * 下载文件：浏览器访问这个地址会弹出保存对话框
     *
     * <p>注意返回值是 void，因为这里我们自己往 HttpServletResponse 里写字节流，
     * 不再走统一的 R 结果包装。
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        writeFileToResponse(id, "attachment", response);
    }

    /**
     * 在线预览 / 在线播放：和下载唯一的区别是响应头用 inline，
     * 浏览器会直接在页面里打开（图片显示、音频播放、歌词直接看），不会弹下载框。
     *
     * <p>提示：这里没有实现 HTTP Range（断点续传/拖动进度条），
     * 普通播放没问题，若要支持音频拖动进度条，后续可再加 Range 处理。
     */
    @GetMapping("/preview/{id}")
    public void preview(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        writeFileToResponse(id, "inline", response);
    }

    // ==================== 查询 ====================

    /**
     * 查询单个文件信息
     */
    @GetMapping("/info/{id}")
    public R<FileMetadata> info(@PathVariable("id") Long id) {
        return R.ok(fileService.getAvailableById(id));
    }

    /**
     * 分页查询文件列表
     *
     * @param current 第几页，默认 1
     * @param size    每页几条，默认 10
     * @param bizType 按业务类型筛选，可填 image/song/lyric/file，不填就是全部
     */
    @GetMapping("/page")
    public R<Page<FileMetadata>> page(@RequestParam(value = "current", defaultValue = "1") long current,
                                      @RequestParam(value = "size", defaultValue = "10") long size,
                                      @RequestParam(value = "bizType", required = false) String bizType,
                                      @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        Page<FileMetadata> page = new Page<>(current, size);
        LambdaQueryWrapper<FileMetadata> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(bizType), FileMetadata::getBizType, bizType)
                .eq(uploaderId != null, FileMetadata::getUploaderId, uploaderId)
                .orderByDesc(FileMetadata::getCreateTime);
        return R.ok(fileService.page(page, wrapper));
    }

    // ==================== 删除 ====================

    /**
     * 删除文件
     *
     * @param physical 是否连磁盘文件一起删除，默认 false（只删数据库记录，方便误删后找回）
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable("id") Long id,
                          @RequestParam(value = "physical", defaultValue = "false") boolean physical) {
        fileService.deleteFile(id, physical);
        return R.ok(null, "删除成功");
    }

    // ==================== 私有工具方法 ====================

    /**
     * 把文件字节流写进响应
     *
     * @param disposition attachment=下载，inline=预览
     */
    private void writeFileToResponse(Long id, String disposition, HttpServletResponse response) throws IOException {
        // 1. 先查数据库，拿到文件信息（不存在会直接抛「文件不存在」）
        FileMetadata metadata = fileService.getAvailableById(id);
        // 2. 再定位磁盘上的真实文件
        File file = fileService.getDiskFile(metadata);

        // 3. 设置响应头
        response.reset();
        // 内容类型：数据库里存的 MIME 类型，拿不到就退化为通用二进制流
        response.setContentType(StringUtils.hasText(metadata.getContentType())
                ? metadata.getContentType() : "application/octet-stream");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 告诉浏览器：这是一个附件(或内联资源)，以及下载出来的文件名
        response.setHeader("Content-Disposition", buildContentDisposition(disposition, metadata.getOriginalName()));
        // 用磁盘上真实的大小，比数据库里的值更可靠
        response.setContentLengthLong(file.length());

        // 4. 写数据流。transferTo 会把输入流直接搬到输出流，不用自己写循环读字节
        try (InputStream inputStream = Files.newInputStream(file.toPath());
             OutputStream outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            // 用户下载到一半取消连接会报这个错，属于正常情况，记日志即可
            log.warn("[文件服务] 文件写出异常, id={}, err={}", id, e.getMessage());
        }
    }

    /**
     * 构造 Content-Disposition 响应头
     *
     * <p>中文文件名必须编码，否则下载出来的文件名是乱码。
     * 这里同时给出 filename（兼容老浏览器）和 filename*（标准写法，支持中文）。
     */
    private String buildContentDisposition(String disposition, String originalName) {
        String encoded = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded;
    }

    /**
     * 确定上传者 id
     *
     * <p>优先级：
     * <ol>
     *   <li>网关鉴权通过后放进请求头的 X-User-Id（正常业务走这条路，最可信）</li>
     *   <li>请求参数里手传的 uploaderId（直连服务、Postman 调试时用）</li>
     * </ol>
     * 都没有就报错，避免数据库 uploader_id 非空约束被破坏。
     */
    private Long resolveUploaderId(Long uploaderIdParam, HttpServletRequest request) {
        String headerUserId = request.getHeader("X-User-Id");
        if (StringUtils.hasText(headerUserId)) {
            try {
                return Long.valueOf(headerUserId.trim());
            } catch (NumberFormatException e) {
                log.warn("[文件服务] 请求头 X-User-Id 不是数字: {}", headerUserId);
            }
        }
        if (uploaderIdParam != null) {
            return uploaderIdParam;
        }
        throw new GlobalException(ResultCodeEnum.PARAM_EMPTY.getCode(), "无法确定上传者，请携带 X-User-Id 请求头");
    }
}
