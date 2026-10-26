package com.musicdreamer.common.exception;
import com.musicdreamer.common.exception.BusinessException;
import com.musicdreamer.common.result.R;
import com.musicdreamer.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常
     */
    @ExceptionHandler(GlobalException.class)
    public R<?> handleGlobalException(GlobalException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public R<?> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数校验异常：{}", msg, e);
        return R.fail(ResultCodeEnum.PARAM_ERROR.getCode(), msg);
    }
    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e) {
        // 这里的 Result 是你项目里的统一返回类，根据你的实际代码调整
        // 注意：这里返回的 code 是 400 或者其他业务码，而不是 500
        return R.fail(e.getCode(), e.getMessage());
    }


    /**
     * 兜底捕获所有未知异常
     */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统未知异常：{}", e.getMessage(), e);
        return R.fail(ResultCodeEnum.FAIL);
    }
}
