package com.mok.framework.mail.service;

import cn.hutool.extra.mail.MailAccount;

/**
 * SMTP 投递边界。
 * 通过接口隔离实际投递与日志持久化，便于分别验证两类结果。
 */
public interface MailTransport {

    void send(MailAccount account, String recipient, String subject, String content, boolean html);
}
