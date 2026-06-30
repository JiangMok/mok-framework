package com.mok.framework.mail.config;

import cn.hutool.extra.mail.MailAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfiguration {

    @Bean
    public MailAccount mailAccount(MailProperties mailProperties) {
        MailAccount account = new MailAccount();
        account.setHost(mailProperties.getHost());
        account.setPort(mailProperties.getPort());
        account.setSslEnable(mailProperties.isSslEnable());
        account.setStarttlsEnable(mailProperties.isStarttlsEnable());
        account.setFrom(mailProperties.getFrom());
        account.setUser(mailProperties.getUser());
        account.setPass(mailProperties.getPassword());
        account.setAuth(true);
        return account;
    }
}