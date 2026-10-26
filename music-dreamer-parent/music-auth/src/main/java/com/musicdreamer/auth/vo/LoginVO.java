package com.musicdreamer.auth.vo;

import lombok.Builder;
import lombok.Data;
//返回给前端的数据
/**
 * 登录返回结果
 */
@Data
@Builder
public class LoginVO {

    /** JWT token */
    private String token;

    /** 用户 id */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 角色 0普通用户 1管理员 */
    private Integer role;

    /** token 过期时间（秒） */
    private Long expiresIn;
}