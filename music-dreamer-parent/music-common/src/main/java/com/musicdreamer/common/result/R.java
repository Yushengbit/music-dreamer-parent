package com.musicdreamer.common.result;


import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class R<T> {

    // 响应码
    private Integer code;
    // 提示消息
    private String msg;
    // 返回数据
    private T data;

    // 成功静态方法
    public static <T> R<T> ok() {
        return restResult(null, ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg());
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg());
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, ResultCodeEnum.SUCCESS.getCode(), msg);
    }

    // 失败静态方法
    public static <T> R<T> fail() {
        return restResult(null, ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMsg());
    }

    public static <T> R<T> fail(String msg) {
        return restResult(null, ResultCodeEnum.FAIL.getCode(), msg);
    }

    public static <T> R<T> fail(Integer code, String msg) {
        return restResult(null, code, msg);
    }

    public static <T> R<T> fail(ResultCodeEnum resultCodeEnum) {
        return restResult(null, resultCodeEnum.getCode(), resultCodeEnum.getMsg());
    }

    private static <T> R<T> restResult(T data, Integer code, String msg) {
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
