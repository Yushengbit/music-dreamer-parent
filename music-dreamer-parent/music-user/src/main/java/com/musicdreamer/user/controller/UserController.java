package com.musicdreamer.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdreamer.common.dto.UserDTO;
import com.musicdreamer.common.exception.GlobalException;
import com.musicdreamer.common.result.R;
import com.musicdreamer.common.result.ResultCodeEnum;
import com.musicdreamer.user.entity.User;
import com.musicdreamer.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    /**
     * 根据id查询用户（服务间调用统一返回 UserDTO，避免 password 等敏感字段外泄）
     */
    @GetMapping("/get/{id}")
    public R<UserDTO> getUserById(@PathVariable("id") Long id){
        User user = userService.getById(id);
        if(user == null){
            throw new GlobalException(ResultCodeEnum.USER_NOT_EXIST);
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return R.ok(userDTO);
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    public R<Void> saveUser(@RequestBody User user){
        userService.save(user);
        return R.ok();
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    public R<Void> updateUser(@RequestBody User user){
        userService.updateById(user);
        return R.ok();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> deleteUser(@PathVariable Long id){
        userService.removeById(id);
        return R.ok();
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<Page<User>> pageUser(@RequestParam(value = "current", defaultValue = "10") long current,
                                  @RequestParam(value = "size", defaultValue = "1") long size){
        Page<User> page = new Page<>(current, size);
        Page<User> resPage = userService.page(page, new LambdaQueryWrapper<User>());

        return R.ok(resPage);
    }

    /**
     * 获取"当前登录用户"信息
     * 网关 JwtAuthFilter 已经把 userId 塞进 X-User-Id header，
     * 这里直接读取即可（不需要再校验 token）
     */
    @GetMapping("/me")
    public R<User> me(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getById(userId);

        System.out.println("收到的 X-User-Id: " + userId);
        if (user == null) {
            throw new GlobalException(ResultCodeEnum.USER_NOT_EXIST);
        }
        return R.ok(user);
    }

}
