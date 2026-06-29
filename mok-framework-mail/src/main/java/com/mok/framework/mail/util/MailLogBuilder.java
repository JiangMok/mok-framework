package com.mok.framework.mail.util;

import cn.hutool.core.util.IdUtil;
import com.mok.framework.model.enums.MailType;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.model.entity.MailLog;

import java.time.LocalDateTime;

/**
 * 邮件日志构建工具
 * 用于从不同邮件消息体构建通用的 MailLog 实体
 */
public class MailLogBuilder {

    /**
     * 构建邮件发送日志实体（基础信息，不含发送状态）
     *
     * @param messageId 消息ID
     * @param recipient 收件人
     * @param subject   邮件主题
     * @param content   邮件正文
     * @param mailType  邮件类型枚举
     * @return 预填充的 MailLog 对象
     */
    public static MailLog build(String messageId, String recipient, String subject,
                                String content, MailType mailType) {
        MailLog logEntity = new MailLog();
        logEntity.setId(IdUtil.simpleUUID());
        logEntity.setMessageId(messageId);
        logEntity.setRecipient(recipient);
        logEntity.setSubject(subject);
        logEntity.setContent(content);
        logEntity.setMailType(mailType.getCode());
        logEntity.setSendTime(LocalDateTime.now());
        return logEntity;
    }
}