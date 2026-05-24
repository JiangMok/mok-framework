package com.mok.framework.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @description: MyBatis-Plus配置类
 * @author: JN
 * @date: 2025/12/31
 */
//@Configuration:spring注解,标记这是一个配置类
@Configuration
public class MyBatisPlusConfig  {

    //@Bean注解:声明这是一个spring bean
    //使用@Bean注解可以非常方便地注册自定义的Bean到Spring的IoC容器中
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //创建MyBatis plus拦截器(可添加多个内部拦截器)
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //添加分页插件
        //PaginationInnerInterceptor : 分页内部拦截器
        //DbType.MYSQL : 指定数据库类型为mysql
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        //返回配置好的拦截器
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        //返回一个MetaObjectHandler匿名实现类
        return new MetaObjectHandler() {
            //插入时自动填充
            @Override
            public void insertFill(MetaObject metaObject) {
                //strictInsertFillL严格模式插入填充
                //  参数1:metemetaObject:元数据对象
                //  参数2:字段名
                //  参数3:字段类型
                //  参数4:填充的值
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
            //更新时自动填充
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }

}
