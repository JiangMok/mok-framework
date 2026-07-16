package com.mok.framework.common.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: SwaggerConfig 配置类
 * @author: JN
 * @date: 2026/1/5 11:02
 * @param:
 * @return:
 **/
@Configuration
public class SwaggerConfig  {

    /**
     * @description: 创建并配置OpenAPI对象
     * @author: JN
     * @date: 2026/1/5 11:05
     * @param: []
     * @return: io.swagger.v3.oas.models.OpenAPI
     **/
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                //设置API信息
                // 作用：配置API文档的基本信息
                // 包含标题、描述、版本、联系信息、许可证等
                .info(new Info()
                        .title("Mok Security Framework API")
                        .description("基于Spring Security + JWT的权限管理系统")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Mok")
                                .email("jiangmok@qq.com")
                                .url(""))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("输入JWT Token（格式：Bearer {token}）")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}