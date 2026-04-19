package com.mok.framework.common.utils;

import cn.hutool.extra.mail.MailUtil;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 邮件发送工具
 */
@Component
public class EmailSenderUtils {

    private static final Logger log = LogUtils.getLogger(EmailSenderUtils.class);

    /**
     * 发送普通文本邮件
     *
     * @param recipient 收件人
     * @param subject   主题
     * @param content   正文
     */
    public void sendTextEmail(String recipient, String subject, String content) {
        String result = MailUtil.send(recipient, subject, content, false);
        log.info("========== 邮件发送结果 : {}", result.toString());
    }
}

