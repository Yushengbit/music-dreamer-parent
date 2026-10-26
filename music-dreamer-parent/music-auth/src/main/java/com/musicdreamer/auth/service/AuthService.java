package com.musicdreamer.auth.service;

import com.musicdreamer.auth.dto.LoginDTO;
import com.musicdreamer.auth.dto.RegisterDTO;
import com.musicdreamer.auth.vo.LoginVO;

public interface AuthService {

    /** 注册 */
    void register(RegisterDTO dto);

    /** 登录，返回 token */
    LoginVO login(LoginDTO dto);

    /** 登出：把 token 加入 Redis 黑名单 */
    void logout(String token);
}