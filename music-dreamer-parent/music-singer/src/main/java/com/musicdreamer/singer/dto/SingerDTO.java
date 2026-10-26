package com.musicdreamer.singer.dto;

import lombok.Data;

@Data
public class SingerDTO {

    // 只留前端可能会传的数据
    private Long id;

    /**
     * 关联 sys_user.id
     */
    private Long userId;

    /**
     * 歌手名
     */
    private String singerName;

    /**
     * 歌手头像
     */
    private String singerAvatar;

    /**
     * 歌手简介
     */
    private String introduction;
}
