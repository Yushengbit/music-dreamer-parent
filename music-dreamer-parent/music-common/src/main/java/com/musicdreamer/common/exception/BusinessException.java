package com.musicdreamer.common.exception;

/**
 * 自定义业务异常
 * 注意：一定要继承 RuntimeException（运行时异常），这样在 Service 层抛出时，
 * 才能触发 Spring 的事务回滚（@Transactional），并且不需要写 try-catch。
 */
public class BusinessException extends RuntimeException {

    // 可以加上错误状态码，方便前端区分具体的业务错误
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400; // 默认业务错误状态码
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
