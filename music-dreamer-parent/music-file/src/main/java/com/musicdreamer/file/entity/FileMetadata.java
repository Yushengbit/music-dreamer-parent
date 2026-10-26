package com.musicdreamer.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件元数据实体类：对应数据库表 file_metadata
 *
 * <p>注意：我们只在数据库里存「文件的描述信息」，真正的文件内容存在磁盘上。
 * 数据库记录 + 磁盘文件 一一对应，删除记录时可以选择是否连带删掉磁盘文件。
 */
@Data
@TableName("file_metadata")
public class FileMetadata {

    /**
     * 主键 id，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户上传时的原始文件名（含扩展名），下载时用它作为下载出来的文件名
     */
    private String originalName;

    /**
     * 存在磁盘上的名字：UUID + 扩展名
     * 之所以改名，一是避免中文名/重名冲突，二是避免用户构造恶意文件名
     */
    private String storedName;

    /**
     * 业务类型：image / song / lyric / file
     */
    private String bizType;

    /**
     * 相对存储根目录的路径，例如 image/2026/10/26/abcd1234.png
     * 存相对路径的好处：以后换服务器、换存储根目录，只要搬文件夹即可，不用改数据库
     */
    private String relativePath;

    /**
     * MIME 类型，例如 image/png、audio/mpeg，下载时告诉浏览器这是什么文件
     */
    private String contentType;

    /**
     * 文件字节大小
     */
    private Long size;

    /**
     * 上传者用户 id（来自网关放的 X-User-Id 请求头）
     */
    private Long uploaderId;

    /**
     * 创建时间，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0 未删，1 已删
     * 加了 @TableLogic 后，调用 removeById() 会变成 update set deleted=1，
     * 并且查询时 MyBatis-Plus 会自动带上 deleted=0 条件
     */
    @TableLogic
    private Integer deleted;
}
