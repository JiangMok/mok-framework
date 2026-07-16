package com.mok.framework.ratelimiter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @description:限流模块自动配置类 -> 自动装配限流模块所需的Bean
 * @author: mok
 * @date: 2026/2/25 14:47
**/
// 声明这是一个 spring framework 配置类,将其中的 @Bean 方法 所产生的对象注册为spring容器管理的Bean
@Configuration
// 扫描限流组件下的所有组件
@ComponentScan(basePackages = "com.mok.framework.ratelimiter")
// 启动配置属性绑定,将配置文件中的属性绑定到 RateLimiterProperties
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterAutoConfiguration {

    /**
     * @description:* 配置RedisTemplate
     * @author: mok
     * @date: 2026/2/25 14:50
     * @param: [connectionFactory] -> Redis连接工厂,由 spring 自动注入
     * @return: org.springframework.data.redis.core.RedisTemplate<java.lang.String,java.lang.Object>
     *              |---> 配置好的 RedisTemplate
    **/
    @Bean
    // 当容器中不存在名为 rateLimiterRedisTemplate 的 bean 时才会创建
    @ConditionalOnMissingBean(name = "rateLimiterRedisTemplate")
    // 作用：创建一个专门用于限流模块的RedisTemplate，定制序列化方式
    public RedisTemplate<String, Object> rateLimiterRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        // 创建 RedisTemplate 实例
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);
        
        // 使用 StringRedisSerializer 来序列化和反序列化redis的key值
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // 设置 key 的序列化器
        template.setKeySerializer(stringSerializer);
        // 设置 hash key 的序列化器
        template.setHashKeySerializer(stringSerializer);
        
        // 使用 GenericJackson2JsonRedisSerializer 来序列化和反序列化redis的value值
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        // 设置value 的序列化器
        template.setValueSerializer(jsonSerializer);
        // 设置hash value的序列化器
        template.setHashValueSerializer(jsonSerializer);

        // 初始化模板,检查是否设置了必要的属性
        template.afterPropertiesSet();
        return template;
    }
}