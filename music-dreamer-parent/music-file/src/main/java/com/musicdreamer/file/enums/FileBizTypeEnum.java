package com.musicdreamer.file.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件业务类型枚举
 *
 * <p>本项目把上传的文件分成四类，每一类单独限制「最大体积」和「允许的扩展名」，
 * 也就是所谓的白名单校验：不在名单里的文件类型直接拒绝，避免有人上传 .exe、.jsp 等危险文件。
 *
 * <p>扩展名白名单为空（如 FILE 普通文件）表示不限制类型。
 */
@Getter
public enum FileBizTypeEnum {

    /** 图片：头像、封面等 */
    IMAGE("image", "图片", 5 * 1024 * 1024L,
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp")),

    /** 歌曲：音频文件，体积较大 */
    SONG("song", "歌曲", 50 * 1024 * 1024L,
            Arrays.asList("mp3", "flac", "wav", "m4a", "ogg", "aac")),

    /** 歌词：.lrc 歌词文件或 .txt 文本 */
    LYRIC("lyric", "歌词", 1 * 1024 * 1024L,
            Arrays.asList("lrc", "txt")),

    /** 普通文件：其他杂项文件，这里不做类型限制，只限制大小 */
    FILE("file", "普通文件", 100 * 1024 * 1024L,
            Collections.emptyList());

    /**
     * 类型编码，会存进数据库的 biz_type 字段
     */
    private final String code;

    /**
     * 中文说明
     */
    private final String desc;

    /**
     * 单文件最大字节数
     */
    private final long maxSize;

    /**
     * 允许的扩展名列表（不含点），为空表示不限制
     */
    private final List<String> allowExtensions;

    FileBizTypeEnum(String code, String desc, long maxSize, List<String> allowExtensions) {
        this.code = code;
        this.desc = desc;
        this.maxSize = maxSize;
        this.allowExtensions = allowExtensions;
    }

    /**
     * 根据前端传来的类型字符串找枚举，找不到返回 null（由调用方决定怎么报错）
     */
    public static FileBizTypeEnum of(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (FileBizTypeEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 判断某个扩展名是否被允许
     *
     * @param extension 扩展名，例如 "png"，不带点
     */
    public boolean allowExtension(String extension) {
        // 白名单为空 = 不限制类型
        if (allowExtensions == null || allowExtensions.isEmpty()) {
            return true;
        }
        if (extension == null || extension.isBlank()) {
            return false;
        }
        return allowExtensions.contains(extension.toLowerCase());
    }

    /**
     * 返回最大体积的 MB 文字，用于报错提示
     */
    public String getMaxSizeMbText() {
        return (maxSize / 1024 / 1024) + "MB";
    }
}
