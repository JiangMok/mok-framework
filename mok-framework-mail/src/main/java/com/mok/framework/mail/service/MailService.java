package com.mok.framework.mail.service;


import com.mok.framework.model.entity.MailLog;
import com.mok.framework.model.enums.MailType;

public interface MailService {

    /**
     * 发送邮件（指定单个收件人）
     */
    void sendAndLogMail(String to, String subject, String content, String messageId, MailType mailType, boolean isHtml);

    /**
     * 按邮件类型群发 — 查询订阅了该类型的所有启用收件人，逐个发送
     */
    void sendByMailType(MailType mailType, String subject, String content, boolean isHtml);
}
