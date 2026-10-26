package com.musicdreamer.common.result;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 参数校验
    PARAM_ERROR(400, "参数错误"),
    PARAM_EMPTY(400, "参数不能为空"),

    // 权限认证
    UNAUTHORIZED(401, "未登录或token失效"),
    FORBIDDEN(403, "权限不足"),

    // 资源
    NOT_FOUND(404, "资源不存在"),

    // 业务自定义
    USER_NOT_EXIST(10001, "用户不存在"),
    USER_PASSWORD_ERROR(10002, "密码错误"),
    USERNAME_EXIST(10003, "用户名已存在"),
    USER_DISABLED(10004, "账号已被禁用"),

    // 文件管理 20001-20099
    FILE_NOT_FOUND(20001, "文件不存在"),
    FILE_UPLOAD_ERROR(20002, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(20003, "不支持的文件业务类型"),
    FILE_SIZE_EXCEED(20004, "文件大小超出限制"),
    FILE_DELETE_FORBIDDEN(20005, "无权删除该文件"),

    // 歌手管理 30001-30099
    SINGER_NOT_EXIST(30001, "歌手不存在"),
    SINGER_EXIST(30002, "该用户已是歌手");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
