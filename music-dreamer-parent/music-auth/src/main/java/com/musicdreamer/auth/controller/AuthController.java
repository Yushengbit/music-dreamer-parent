package com.musicdreamer.auth.controller;

import com.musicdreamer.auth.dto.LoginDTO;
import com.musicdreamer.auth.dto.RegisterDTO;
import com.musicdreamer.auth.service.AuthService;
import com.musicdreamer.auth.vo.LoginVO;
import com.musicdreamer.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     * POST /auth/register
     */
    @PostMapping("/register")
    public R<Void> register(@RequestBody RegisterDTO dto) {
        authService.register(dto);
        return R.ok();
    }

    /**
     * 用户登录
     * POST /auth/login
     */
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    /**
     * 用户登出
     * POST /auth/logout
     * 注意：此接口【不在】白名单，必须携带有效 token 才能访问
     * 逻辑：把当前 token 的 jti 写入 Redis 黑名单，后续该 token 一律被网关拦截
     */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7).trim());
        }
        return R.ok();
    }
}