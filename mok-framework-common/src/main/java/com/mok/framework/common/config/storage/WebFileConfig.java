package com.mok.framework.common.config.storage;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebFileConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传目录不能直接暴露；公开头像由文件模块按数据库业务类型受控读取。
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
