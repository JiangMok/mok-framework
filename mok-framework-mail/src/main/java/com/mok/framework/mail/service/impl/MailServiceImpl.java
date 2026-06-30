package com.mok.framework.mail.service.impl;

import cn.hutool.extra.mail.Mail;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.MailLogBuilder;
import com.mok.framework.model.entity.MailLog;
import com.mok.framework.model.enums.MailType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 *
 * @author: mok
 * @date: 2026/6/30
 */
@Service
public class MailServiceImpl implements MailService {

    private final MailAccount mailAccount;
    private final MailLogService mailLogService;

    public MailServiceImpl(MailAccount mailAccount,
                           MailLogService mailLogService) {
        this.mailAccount = mailAccount;
        this.mailLogService = mailLogService;
    }

    @Override
    public void sendAndLogMail(String to, String subject, String content, String messageId, MailType mailType, boolean isHtml) {
        MailLog mailLog = MailLogBuilder.build(messageId, to, subject, content, mailType);
        try {
            MailUtil.send(mailAccount, to, subject, content, isHtml);
            mailLog.setSendStatus("SUCCESS");
        } catch (Exception e) {
            mailLog.setSendStatus("FAILED");
            mailLog.setFailReason(e.getMessage());
            mailLog.setRetryCount(0);
            throw new RuntimeException("邮件发送失败", e);  // 抛出异常让消费者捕获
        } finally {
            // 无论成功失败，都保存日志（利用 saveOrUpdateByMessageId 处理重复消息）
            mailLogService.saveOrUpdateByMessageId(mailLog);
        }
    }
}
