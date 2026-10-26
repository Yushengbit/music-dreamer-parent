package com.musicdreamer.auth.dto;

import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterDTO {

    /** 用户名 */
    private String username;

    /** 密码（明文） */
    private String password;

    /** 昵称 */
    private String nickname;
}