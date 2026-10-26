package com.musicdreamer.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.common.exception.GlobalException;
import com.musicdreamer.common.result.ResultCodeEnum;
import com.musicdreamer.file.config.FileStorageProperties;
import com.musicdreamer.file.entity.FileMetadata;
import com.musicdreamer.file.enums.FileBizTypeEnum;
import com.musicdreamer.file.mapper.FileMetadataMapper;
import com.musicdreamer.file.service.FileService;
import com.musicdreamer.file.vo.FileUploadVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * 文件服务实现类：本地文件系统存储方案的完整实现
 *
 * <p>整体思路（一句话）：把文件写到磁盘目录，把文件的「描述信息」写进数据库，
 * 以后下载时先查数据库拿到路径，再去磁盘读文件。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMetadataMapper, FileMetadata> implements FileService {

    /**
     * 日期目录格式：按 年/月/日 分目录，避免一个文件夹里堆几万个文件
     */
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileStorageProperties storageProperties;

    /**
     * 存储根目录的绝对路径，服务启动时初始化一次
     */
    private Path storageRoot;

    /**
     * @PostConstruct：对象创建完、依赖注入完成后立刻执行，相当于「初始化方法」。
     * 这里把 yml 里配置的路径转成绝对路径，并提前把根目录建好。
     */
    @PostConstruct
    public void init() {
        this.storageRoot = Paths.get(storageProperties.getStoragePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageRoot);
            log.info("[文件服务] 存储根目录已就绪: {}", this.storageRoot);
        } catch (IOException e) {
            // 目录建不了就没法干活，直接让服务启动失败，方便你第一时间发现配置写错了
            throw new IllegalStateException("[文件服务] 存储根目录创建失败: " + this.storageRoot, e);
        }
    }

    /**
     * ========== 上传 ==========
     *
     * <p>步骤：判空 → 校验类型 → 校验大小 → 校验扩展名 → 生成存储路径 → 写磁盘 → 存数据库
     */
    @Override
    public FileUploadVO upload(MultipartFile multipartFile, String bizTypeCode, Long uploaderId) {
        // 1. 文件不能为空
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new GlobalException(ResultCodeEnum.PARAM_EMPTY.getCode(), "上传文件不能为空");
        }
        if (uploaderId == null) {
            throw new GlobalException(ResultCodeEnum.PARAM_EMPTY.getCode(), "缺少上传者 id");
        }

        // 2. 业务类型必须是我们支持的四种之一
        FileBizTypeEnum bizType = FileBizTypeEnum.of(bizTypeCode);
        if (bizType == null) {
            throw new GlobalException(ResultCodeEnum.FILE_TYPE_NOT_SUPPORTED.getCode(),
                    "不支持的文件类型：" + bizTypeCode + "，请使用 image/song/lyric/file");
        }

        // 3. 处理原始文件名
        //    cleanPath 会把 a/../../b 这种恶意路径规整掉；
        //    再截取最后一个 / 或 \ 后面的部分，保证拿到的只是一个纯文件名，不含任何目录
        String rawName = StringUtils.cleanPath(
                Objects.requireNonNullElse(multipartFile.getOriginalFilename(), ""));
        int slashIndex = Math.max(rawName.lastIndexOf('/'), rawName.lastIndexOf('\\'));
        String originalName = slashIndex >= 0 ? rawName.substring(slashIndex + 1) : rawName;
        if (!StringUtils.hasText(originalName)) {
            originalName = "unnamed";
        }

        // 4. 取扩展名（不含点），例如 "PNG" → "png"
        String extension = Objects.requireNonNullElse(
                StringUtils.getFilenameExtension(originalName), "").toLowerCase();

        // 5. 体积校验：注意 yml 里的 spring.servlet.multipart.max-file-size 也要同步放开，
        //    否则超大文件在到达这里之前就被 Tomcat 拦掉了
        long size = multipartFile.getSize();
        if (size > bizType.getMaxSize()) {
            throw new GlobalException(ResultCodeEnum.FILE_SIZE_EXCEED.getCode(),
                    "文件大小超出限制，" + bizType.getDesc() + "最大支持 " + bizType.getMaxSizeMbText());
        }

        // 6. 扩展名白名单校验，挡掉 .exe/.jsp 等危险文件
        if (!bizType.allowExtension(extension)) {
            throw new GlobalException(ResultCodeEnum.FILE_TYPE_NOT_SUPPORTED.getCode(),
                    "不允许上传 ." + extension + " 文件，" + bizType.getDesc() + "仅支持：" + bizType.getAllowExtensions());
        }

        // 7. 拼存储路径： 根目录 / 业务类型 / 年 / 月 / 日 / UUID.扩展名
        //    例：D:/uploads/image/2026/10/26/550e8400e29b41d4a716446655440000.png
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (extension.isEmpty() ? "" : "." + extension);
        String relativePath = bizType.getCode() + "/" + datePath + "/" + storedName;

        Path target = this.storageRoot.resolve(relativePath).normalize();
        // 安全兜底：最终路径必须还在存储根目录里面
        if (!target.startsWith(this.storageRoot)) {
            throw new GlobalException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }

        // 8. 写磁盘。createDirectories 会连带创建 年/月/日 各级目录，不存在则不创建
        try {
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("[文件服务] 文件写入磁盘失败, path={}", target, e);
            throw new GlobalException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }

        // 9. 保存元数据到数据库
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalName(originalName);
        metadata.setStoredName(storedName);
        metadata.setBizType(bizType.getCode());
        metadata.setRelativePath(relativePath);
        metadata.setContentType(multipartFile.getContentType());
        metadata.setSize(size);
        metadata.setUploaderId(uploaderId);
        try {
            this.save(metadata);
        } catch (Exception e) {
            // 数据库写失败了，把刚写的磁盘文件删掉，避免出现没人认领的垃圾文件
            deleteQuietly(target);
            log.error("[文件服务] 文件元数据保存失败, path={}", target, e);
            throw new GlobalException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }

        // 10. 组装返回给前端的数据
        FileUploadVO vo = new FileUploadVO();
        BeanUtils.copyProperties(metadata, vo);
        vo.setDownloadUrl(buildUrl("/download/", metadata.getId()));
        vo.setPreviewUrl(buildUrl("/preview/", metadata.getId()));
        return vo;
    }

    /**
     * 根据 id 查询文件记录，查不到直接抛业务异常（由全局异常处理器转成统一 JSON）
     */
    @Override
    public FileMetadata getAvailableById(Long id) {
        FileMetadata metadata = this.getById(id);
        if (metadata == null) {
            throw new GlobalException(ResultCodeEnum.FILE_NOT_FOUND);
        }
        return metadata;
    }

    /**
     * 把数据库里的相对路径还原成磁盘上的真实文件
     *
     * <p>为什么还要校验一次？因为数据库里的路径理论上有可能被人为改坏，
     * 如果不校验，构造出 ../../ 就能读到存储目录之外的文件（目录穿越漏洞）。
     */
    @Override
    public File getDiskFile(FileMetadata metadata) {
        Path target = this.storageRoot.resolve(metadata.getRelativePath()).normalize();
        if (!target.startsWith(this.storageRoot)) {
            log.warn("[文件服务] 检测到非法文件路径: {}", metadata.getRelativePath());
            throw new GlobalException(ResultCodeEnum.FILE_NOT_FOUND);
        }
        File file = target.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new GlobalException(ResultCodeEnum.FILE_NOT_FOUND);
        }
        return file;
    }

    /**
     * 删除文件
     */
    @Override
    public void deleteFile(Long id, boolean physical) {
        FileMetadata metadata = this.getAvailableById(id);
        // 逻辑删除：因为实体类上加了 @TableLogic，这里实际执行的是 update set deleted=1
        this.removeById(id);
        // physical=true 时才真的删掉磁盘文件；默认只删记录，给误删留后悔药
        if (physical) {
            File file = this.getDiskFile(metadata);
            deleteQuietly(file.toPath());
        }
    }

    /**
     * 拼接对外访问 URL，前缀来自 yml 的 music.file.access-prefix
     */
    private String buildUrl(String suffix, Long id) {
        String prefix = storageProperties.getAccessPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "";
        }
        return prefix + suffix + id;
    }

    /**
     * 安静地删除文件：删不掉也不抛异常，只记日志
     */
    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("[文件服务] 文件删除失败, path={}", path, e);
        }
    }
}
