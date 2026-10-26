package com.musicdreamer.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息传输对象（服务间调用契约）
 * 字段与 music-user 的 User 实体保持一致，
 * 但不包含 password 等敏感字段，避免用户信息外泄
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0禁用 1正常
     */
    private Integer status;

    /**
     * 角色 0普通用户 1管理员
     */
    private Integer role;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
