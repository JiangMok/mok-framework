package com.mok.framework.common.annotation;


import java.lang.annotation.*;

//指定该注解只能用于方法上
@Target(ElementType.METHOD)
//注解保留到运行时,便于通过反射读取
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventDuplicate {

    /**
     * 防重复提交的Key，支持SpEL表达式
     * 例如：#user.id + '-' + #request.type
     * 作用:自定义唯一标识,用于区分不同的提交场景
     */
    String key() default "";

    /**
     * 操作类型
     * todo 可以升级为枚举类,当前只是测试
     */
    String type() default "60";

    /**
     * 锁定的时间（秒）
     * 作用:在该时间段内,相同的key不允许再次提交
     */
    int lockTime() default 3;

    /**
     * 提示信息
     */
    String message() default "";
}
