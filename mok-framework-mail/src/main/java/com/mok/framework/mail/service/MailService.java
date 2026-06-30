package com.mok.framework.mail.service;


import com.mok.framework.model.entity.MailLog;
import com.mok.framework.model.enums.MailType;

public interface MailService {

    /**
     * @description:  发送邮件
     * @author: mok
     * @date: 2026/6/30 13:44
     * @param: [to, subject, content, isHtml]
     * @return: void
    **/
    void sendAndLogMail(String to, String subject, String content, String messageId, MailType mailType, boolean isHtml);
}
