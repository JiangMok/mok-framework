package com.mok.framework.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.mok.framework.common.constant.ResponseCode;
import com.mok.framework.common.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description: 全局异常处理器
 * @author: JN
 * @date: 2026/1/1 00:04
 * @param:
 * @return:
 **/

//@RestControllerAdvice 声明这是一个全局异常处理器
//这个注解是 @ControllerAdvice 和 @ResponseBody 的组合
//表示这个类会处理所有Controller抛出的异常
@RestControllerAdvice
public class GlobalExceptionHandler  {
       private static final Logger log = LogUtils.getLogger(GlobalExceptionHandler.class);

    /**
     * @description: 业务异常
     * @author: JN
     * @date: 2026/1/1 09:57
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @ExceptionHandler(BusinessException.class)
    public R<String> handleBusinessException(BusinessException e,
                                             HttpServletRequest request) {
        log.warn("业务异常，请求地址：{}，异常信息：{}",
                request.getRequestURI(), e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<String> handleNotLoginException(NotLoginException e,
                                             HttpServletRequest request) {
        log.warn("token异常，请求地址：{}，异常信息：{}",
                request.getRequestURI(), e.getMessage());
        return R.tokenInvalid();
    }

    /**
     * @description: 参数验证异常
     *                  MethodArgumentNotValidException 通常在 @Valid 注解参数验证失败时抛出
     * @author: JN
     * @date: 2026/1/1 10:00
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public R<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                           HttpServletRequest request) {
        //获取所有字段验证错误
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        //将字段错误信息拼接成字符串
        //  格式 : 字段名1:错误信息1;字段名2:错误信息2
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数验证异常，请求地址：{}，异常信息：{}", request.getRequestURI(), message);
        //创建统一参数验证错误响应
        return R.validationError(message);
    }

    /**
     * @description: 处理参数绑定异常
     *                  BindException 通常在 @ModelAttribute 注解的参数绑定失败时抛出
     * @author: JN
     * @date: 2026/1/1 10:04
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    @ExceptionHandler(BindException.class)
    public R<String> handleBindException(BindException e, HttpServletRequest request) {
        //获取所有字段绑定错误
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        //将字段绑定的错误信息拼接成字符串
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常，请求地址：{}，异常信息：{}", request.getRequestURI(), message);
        //返回统一响应
        return R.validationError(message);
    }

    /**
     * @description: 处理约束违反异常
     *                  ConstraintViolationException 通常在 @Validated 注解的方法参数验证功能失败时抛出
     * @author: JN
     * @date: 2026/1/1 10:06
     * @param: [e, request]
     * @return:
    **/
    @ExceptionHandler(ConstraintViolationException.class)
    public R<String> handleConstraintViolationException(ConstraintViolationException e,
                                                        HttpServletRequest request) {
        //获取所有违反约束的信息
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        //将约束违反信息拼接成字符串
        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("约束违反异常，请求地址：{}，异常信息：{}", request.getRequestURI(), message);
        //返回统一错误响应
        return R.validationError(message);
    }

    /**
     * @description: 处理缺少请求参数异常
     *                  MissingServletRequestParameterException在请求中缺少必须参数时抛出
     * @author: JN
     * @date: 2026/1/1 10:08
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<String> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        //构建错误信息
        String message = "缺少必要参数: " + e.getParameterName();
        log.warn("参数缺失异常，请求地址：{}，异常信息：{}", request.getRequestURI(), message);
        //返回参数缺少统一响应
        //  使用R.badRequest创建400错误响应
        return R.badRequest(message);
    }
    
    /**
     * @description: 处理方法参数类型不匹配异常
     *
     * @author: JN
     * @date: 2026/1/1 10:10
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<String> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        //构建错误信息
        String message = String.format("参数类型错误: %s 应该是 %s 类型",
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型不匹配异常，请求地址：{}，异常信息：{}", request.getRequestURI(), message);
        //返回统一错误响应
        return R.badRequest(message);
    }

    /**
     * @description: 处理访问被拒绝异常
     *                  AccessDeniedException 在用户已经认证但是没有足够权限时抛出
     * @author: JN
     * @date: 2026/1/1 10:15
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    @ExceptionHandler(NotPermissionException.class)
    public R<String> handleNotPermissionException(NotPermissionException e,
                                                 HttpServletRequest request) {
        log.warn("权限不足异常，请求地址：{}，异常信息：{}",
                request.getRequestURI(), e.getMessage());
        return R.forbidden("权限不足，无法访问该资源");
    }

    /**
     * @description: 处理 HTTP 请求方法不支持异常
     *                  HttpRequestMethodNotSupportedException在请求方法不被支持时抛出
     * @author: JN
     * @date: 2026/1/1 10:17
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<String> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = String.format("不支持 %s 请求方法，支持的方法: %s",
                e.getMethod(), e.getSupportedHttpMethods());
        log.warn("请求方法不支持异常，请求地址：{}，异常信息：{}", request.getRequestURI(), message);
        //返回统一响应:方法不允许
        return R.methodNotAllowed(message);
    }

    /**
     * @description: 处理没有找到处理程序异常
     *                  NoHandlerFoundException 在请求的 URL 没有对应的 Controller 方法时抛出
     *                  注意 : 需要配置spring.mvc.throw-exception-if-no-handler-found=true
     *                        新版本 spring boot : spring.web.throw-exception-if-no-handler-found=true
     * @author: JN
     * @date: 2026/1/1 10:19
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    @ExceptionHandler(NoHandlerFoundException.class)
    //设置HTTP状态码为404
    //这个注解会覆盖默认的HTTP状态码
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<String> handleNoHandlerFoundException(NoHandlerFoundException e,
                                                   HttpServletRequest request) {
        String message = String.format("接口不存在: %s %s",
                e.getHttpMethod(), e.getRequestURL());
        log.warn("接口不存在异常，请求地址：{}，异常信息：{}", request.getRequestURI(), message);
        //返回统一接口不存在响应
        return R.notFound(message);
    }

    /**
     * @description: 数据库异常
     * @author: JN
     * @date: 2026/1/1 10:28
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    //SQLException 在数据库操作失败时抛出
    @ExceptionHandler(SQLException.class)
    public R<String> handleSQLException(SQLException e, HttpServletRequest request) {
        //使用 error 级别记录,因为数据库异常通常比较严重
        log.error("数据库异常，请求地址：{}", request.getRequestURI(), e);

        // 可以根据不同的 SQL 状态码返回不同的错误信息
        String sqlState = e.getSQLState();
        String message = "数据库操作失败";

        if ("23000".equals(sqlState)) {
            message = "数据重复或违反唯一约束";
        } else if ("22001".equals(sqlState)) {
            message = "数据过长";
        } else if ("23505".equals(sqlState)) {
            message = "违反唯一约束条件";
        } else if ("23503".equals(sqlState)) {
            message = "违反外键约束条件";
        }
        //返回统一错误响应
        return R.error(ResponseCode.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * @description:    系统异常
     * @author: JN
     * @date: 2026/1/1 10:30
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    //Exception 处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e, HttpServletRequest request) {
        //使用 error 级别记录,因为这是未预期的异常
        log.error("系统异常，请求地址：{}", request.getRequestURI(), e);
        //生产环境不返回详细错误信息
        //  出于安全考虑,避免暴露系统内部信息
        String message = "系统异常，请联系管理员";
        // 开发环境返回详细错误
        if (isDevEnvironment()) {
            message = e.getMessage();
        }
        //返回统一错误响应
        return R.error(ResponseCode.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * @description:  运行时异常
     * @author: JN
     * @date: 2026/1/1 10:43
     * @param: [e, request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
    **/
    //专门处理 RuntimeException 及其子类
    //注意:这个处理器的优先级高于 Exception.class 处理器
    @ExceptionHandler(RuntimeException.class)
    public R<String> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常，请求地址：{}", request.getRequestURI(), e);
        // 可以根据不同的 RuntimeException 类型返回不同的错误信息
        if (e instanceof IllegalArgumentException) {
            //非法参数异常
            return R.badRequest(e.getMessage());
        } else if (e instanceof IllegalStateException) {
            //非法状态异常
            return R.error(ResponseCode.INTERNAL_SERVER_ERROR, "系统状态异常");
        } else if (e instanceof NullPointerException) {
            //空指针异常
            //空指针异常通常代表代码逻辑错误,需要特别注意
            log.error("空指针异常，请检查代码逻辑", e);
            return R.error(ResponseCode.INTERNAL_SERVER_ERROR, "系统内部错误");
        } else {
            //处理其他运行时异常
            return R.error(ResponseCode.INTERNAL_SERVER_ERROR, "系统运行时异常");
        }
    }

    /**
     * @description: 判断是否是开发环境 
     * @author: JN
     * @date: 2026/1/1 10:39
     * @param: []
     * @return: boolean
    **/
    private boolean isDevEnvironment() {
        // 这里可以根据配置文件判断是否是开发环境
        // 实际项目中可以从配置文件中读取
        String env = System.getProperty("spring.profiles.active", "dev");
        return "dev".equals(env) || "test".equals(env);
    }
}