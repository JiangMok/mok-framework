package com.mok.framework.mail.config;

import org.springframework.context.annotation.Configuration;

/**
 * 邮件模块配置
 * MailAccount 改为由 MailSenderService 从 DB 动态构建（支持热刷新），
 * 此处不再创建 MailAccount Bean
 *
 * @author mok
 * @date 2026/6/30
 */
@Configuration
public class MailConfiguration {
    // MailAccount 已移至 MailSenderServiceImpl 中动态管理（AtomicReference 热刷新）
}
