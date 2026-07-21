package com.mok.framework.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author: mok
 * @date: 2026/7/21
 */
@Component
@ConfigurationProperties(prefix = "mok.start")
public class SystemStartConfig {

    // 系统启动的邮件发送,默认不发送
    private boolean systemStartCheckMail = false;

    public boolean getSystemStartCheckMail() {
        return systemStartCheckMail;
    }

    public void setSystemStartCheckMail(boolean systemStartCheckMail) {
        this.systemStartCheckMail = systemStartCheckMail;
    }

    @Override
    public String toString() {
        return "SystemStartConfig{" +
                "systemStartCheckMail=" + systemStartCheckMail +
                '}';
    }
}
