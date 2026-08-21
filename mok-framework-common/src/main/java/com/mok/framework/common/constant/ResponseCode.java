package com.mok.framework.common.constant;

/**
 * @description: 响应码常量类
 * @author: JN
 * @date: 2025/12/31 23:43
 * @param:
 * @return:
**/
public interface ResponseCode {
    
    // =================== 成功响应码 ===================
    Integer SUCCESS = 200;
    String SUCCESS_MSG = "操作成功";
    
    // =================== 客户端错误码 ===================
    Integer BAD_REQUEST = 400;
    String BAD_REQUEST_MSG = "请求参数错误";
    
    Integer UNAUTHORIZED = 401;
    String UNAUTHORIZED_MSG = "未认证或认证已过期";
    
    Integer FORBIDDEN = 403;
    String FORBIDDEN_MSG = "权限不足";
    
    Integer NOT_FOUND = 404;
    String NOT_FOUND_MSG = "资源不存在";
    
    Integer METHOD_NOT_ALLOWED = 405;
    String METHOD_NOT_ALLOWED_MSG = "请求方法不允许";
    
    // =================== 服务器错误码 ===================
    Integer INTERNAL_SERVER_ERROR = 500;
    String INTERNAL_SERVER_ERROR_MSG = "服务器内部错误";
    
    Integer SERVICE_UNAVAILABLE = 503;
    String SERVICE_UNAVAILABLE_MSG = "服务暂时不可用";
    
    // =================== 业务错误码 ===================
    Integer BUSINESS_ERROR = 1000;
    String BUSINESS_ERROR_MSG = "业务异常";
    
    Integer VALIDATION_ERROR = 1001;
    String VALIDATION_ERROR_MSG = "参数验证失败";
    
    Integer CAPTCHA_ERROR = 1002;
    String CAPTCHA_ERROR_MSG = "验证码错误";

    Integer RATE_LIMIT_ERROR = 1003;
    String RATE_LIMIT_ERROR_MSG = "请求过于频繁";

    Integer DUPLICATE_SUBMIT_ERROR = 1004;
    String DUPLICATE_SUBMIT_ERROR_MSG = "请勿重复提交";
    
    Integer USER_NOT_FOUND = 2001;
    String USER_NOT_FOUND_MSG = "用户不存在";
    
    Integer USER_DISABLED = 2002;
    String USER_DISABLED_MSG = "用户已被禁用";
    
    Integer PASSWORD_ERROR = 2003;
    String PASSWORD_ERROR_MSG = "用户名或密码错误";
    
    Integer TOKEN_EXPIRED = 3001;
    String TOKEN_EXPIRED_MSG = "Token已过期";
    
    Integer TOKEN_INVALID = 3002;
    String TOKEN_INVALID_MSG = "Token无效";
}
