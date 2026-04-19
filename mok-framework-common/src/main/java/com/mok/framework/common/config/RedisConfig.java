package com.mok.framework.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
/**
 * @description: Redis配置类
 * @author: JN
 * @date: 2025/12/31
 */
//@Configuration:spring注解,标记这是一个配置类
@Configuration
public class RedisConfig  {

    @Bean
    @Primary
    public RedisTemplate<String ,Object> redisTemplate(RedisConnectionFactory connectionFactory){
        //创建RedisTemplate实例
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        //设置redis连接工厂
        //  作用:告诉RedisTemplate如何连接redis服务器
        template.setConnectionFactory(connectionFactory);
        //创建jackson json序列化器
        //  作用:将Java对象序列化为json字符串存储到redis
        //  有点:可读性好,支持复杂对象,跨语言兼容
        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer();

        //设置普通key的序列化方式------------------
        //  StringRedisSerializer:将字符串序列化为字节数组
        //  作用:使redis中的key保持为可读的字符串格式
        template.setKeySerializer(new StringRedisSerializer());
        //设置hash结构的key的方式-----------------
        //  作用:使Hash结构中的field保持为可读的字符串格式
        template.setHashKeySerializer(new StringRedisSerializer());

        //设置普通value的序列化方式++++++++++++++++++
        //     GenericJackson2JsonRedisSerializer：将对象序列化为JSON
        //     作用：使Redis中的value存储为JSON格式，便于阅读和调试
        template.setValueSerializer(jacksonSerializer);
        //设置Hash结构的value的序列化方式++++++++++++++++++
        //     作用：使Hash结构中的value也存储为JSON格式
        template.setHashValueSerializer(jacksonSerializer);

        //初始化模板配置
        //  作用:在设置完所有属性后,必须调用此方法初始化RedisTemplate
        //  注意:如果不调用,RedisTemplate可能无法正常工作
        template.afterPropertiesSet();
        //返回配置好的RedisTemplate实例
        //  这个实例将被spring容器管理,可以在其他地方通过@Autowired注入使用
        return template;
    }

}
