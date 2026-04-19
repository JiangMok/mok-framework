package com.mok.framework.common.annotation;

import com.mok.framework.common.enums.BusinessType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
// 注解可以用在哪里
@Target({ElementType.METHOD})
// 注解能活到什么时候
@Retention(RetentionPolicy.RUNTIME)
// 生成文档时要不要显示
@Documented
public @interface OperationLog {
    // 接口标题
    String title() default "";

    // 操作类型
    BusinessType businessType() default BusinessType.OTHER;

    //是否保存请求参数
    boolean saveRequestParam() default true;

    //是否保存响应参数
    boolean saveResponseData() default true;
}
