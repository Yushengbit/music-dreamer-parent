package com.musicdreamer.user.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserDTO {

    //只留前端可能会传的数据
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

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

//    /**
//     * 状态 0禁用 1正常
//     */
//    private Integer status;
//
//    /**
//     * 角色 0普通用户 1管理员
//     */
//    private Integer role;
//
//    /**
//     * 创建时间
//     */
//    @TableField(fill = FieldFill.INSERT)
//    private LocalDateTime createTime;
//
//    /**
//     * 更新时间
//     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
//    private LocalDateTime updateTime;
//
//    /**
//     * 逻辑删除 0未删 1已删
//     */
//    @TableLogic
//    private Integer deleted;
}
