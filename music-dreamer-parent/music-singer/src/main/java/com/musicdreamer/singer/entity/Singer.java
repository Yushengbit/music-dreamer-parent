package com.musicdreamer.singer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("singer")
public class Singer {

    @TableId(type = IdType.AUTO)
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

    /**
     * 状态 0禁用 1正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Integer deleted;
}
