package cn.com.mfish.common.core.advice;

import cn.com.mfish.common.core.exception.MyRuntimeException;
import cn.com.mfish.common.core.exception.OAuthValidateException;
import cn.com.mfish.common.core.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;

/**
 * @author: mfish
 * @description: 全局异常处理
 * @date: 2021/12/13 18:06
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {
    /**
     * 认证异常
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(OAuthValidateException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Integer> exception(OAuthValidateException exception) {
        log.error("401校验异常", exception);
        return Result.fail(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
    }

    /**
     * 禁止访问异常
     *
     * @param exception
     * @return
     */
    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Integer> badRequestException(AccessDeniedException exception) {
        log.error("403异常", exception);
        return Result.fail(HttpStatus.FORBIDDEN.value(), exception.getMessage());
    }

    /**
     * 请求错误异常
     *
     * @param exception
     * @return
     */
    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class
            , HttpMessageNotReadableException.class, UnsatisfiedServletRequestParameterException.class
            , MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Integer> badRequestException(Exception exception) {
        log.error("400异常", exception);
        return Result.fail(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    /**
     * 自定义异常处理
     *
     * @param exception
     * @param request
     * @return
     */
    @ExceptionHandler(MyRuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Integer> myHandleException(Exception exception, HttpServletRequest request) {
        log.error("请求地址'{}',处理异常.", request.getRequestURI(), exception);
        return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
    }

    /**
     * 服务内部异常
     *
     * @param exception
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Integer> handleException(Exception exception, HttpServletRequest request) {
        log.error("请求地址'{}',发生系统异常.", request.getRequestURI(), exception);
        return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "错误:未知异常");
    }
}