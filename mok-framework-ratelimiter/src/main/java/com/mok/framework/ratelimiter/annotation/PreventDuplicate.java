package com.mok.framework.ratelimiter.annotation;

import com.mok.framework.ratelimiter.enums.PreventDuplicateType;

import java.lang.annotation.*;

/**
 * @description: 防重复提交注解类
 * @author: mok
 * @date: 2026/2/26 20:03
**/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventDuplicate {

    /**
     * 防重复提交的Key，支持SpEL表达式
     * 例如：#user.id + '-' + #request.type
     */
    String key() default "";

    /**
     * 锁定的时间（秒）
     */
    int lockTime() default 3;

    /**
     * 提示信息
     */
    String message() default "";

    /**
     * 操作类型
     */
    PreventDuplicateType type() default PreventDuplicateType.DEFAULT;
}