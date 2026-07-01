package com.mok.framework.common.config.datasource;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * 动态数据源配置类
 * 排除默认的数据源自动配置，使用动态数据源
 *
 *  TODO: 2026-04-25
 *  当前使用 @Import 手动导入 DynamicDataSourceAutoConfiguration。
 *  待深入了解 Spring Boot 自动装配机制后，探究为何 dynamic-datasource
 *  的自动配置未生效（是否与 MF 已有配置冲突？是否缺少某个条件注解？）。
 *  未来若能解决，可移除此类。
 *
 * @author mok
 * @date 2026/04/22
 */
/**

 */
@Configuration
// 引入动态数据源自动配置
@Import(DynamicDataSourceAutoConfiguration.class)
public class DynamicDataSourceConfig {
    
}