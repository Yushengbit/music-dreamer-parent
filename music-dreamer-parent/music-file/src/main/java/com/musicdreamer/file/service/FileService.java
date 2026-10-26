package com.musicdreamer.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.file.entity.FileMetadata;
import com.musicdreamer.file.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件服务接口
 *
 * <p>继承 IService 后可以直接用 getById / save / removeById / page 等通用方法。
 */
public interface FileService extends IService<FileMetadata> {

    /**
     * 上传文件并保存元数据
     *
     * @param multipartFile 前端传来的文件
     * @param bizTypeCode   业务类型 image / song / lyric / file
     * @param uploaderId    上传者用户 id
     * @return 上传结果（含访问 URL）
     */
    FileUploadVO upload(MultipartFile multipartFile, String bizTypeCode, Long uploaderId);

    /**
     * 根据 id 查询未被删除的文件记录，查不到就抛「文件不存在」的异常
     */
    FileMetadata getAvailableById(Long id);

    /**
     * 把数据库记录还原成磁盘上真实的 File 对象
     * 内部会做路径安全校验，防止 ../ 这类目录穿越
     */
    File getDiskFile(FileMetadata metadata);

    /**
     * 删除文件
     *
     * @param physical true = 连同磁盘文件一起删掉；false = 只逻辑删除数据库记录
     */
    void deleteFile(Long id, boolean physical);
}
