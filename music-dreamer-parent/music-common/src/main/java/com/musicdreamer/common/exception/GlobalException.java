package com.musicdreamer.common.exception;

import com.musicdreamer.common.result.ResultCodeEnum;
import lombok.Data;

/**
 * 自定义业务异常
 */
@Data
public class GlobalException extends RuntimeException{

    private Integer code;

    public GlobalException(String message) {
        super(message);
        this.code = ResultCodeEnum.FAIL.getCode();
    }

    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public GlobalException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMsg());
        this.code = resultCodeEnum.getCode();
    }
}
