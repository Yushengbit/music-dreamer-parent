package com.musicdreamer.file.vo;

import lombok.Data;

/**
 * 上传成功后的返回对象（VO = View Object，给前端看的数据）
 *
 * <p>里面除了文件的基本信息，还带了下载地址 / 预览地址，
 * 前端拿到后直接把 URL 塞进 &lt;img src&gt;、&lt;audio src&gt; 或 a 标签的 href 即可。
 */
@Data
public class FileUploadVO {

    /**
     * 文件记录 id，之后下载/删除都靠它
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 磁盘存储名
     */
    private String storedName;

    /**
     * 业务类型：image / song / lyric / file
     */
    private String bizType;

    /**
     * MIME 类型
     */
    private String contentType;

    /**
     * 文件字节大小
     */
    private Long size;

    /**
     * 上传者用户 id
     */
    private Long uploaderId;

    /**
     * 下载 URL（浏览器访问会弹出下载框）
     */
    private String downloadUrl;

    /**
     * 在线预览 URL（图片、歌词可直接看，音频可在线播放）
     */
    private String previewUrl;
}
