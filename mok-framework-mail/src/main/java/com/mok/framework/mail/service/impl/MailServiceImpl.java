package com.mok.framework.mail.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.mail.service.MailRecipientService;
import com.mok.framework.mail.service.MailSenderService;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.MailLogBuilder;
import com.mok.framework.model.entity.MailLog;
import com.mok.framework.model.entity.MailRecipient;
import com.mok.framework.model.enums.MailType;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 邮件发送服务实现
 *
 * @author: mok
 * @date: 2026/6/30
 */
@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LogUtils.getLogger(MailServiceImpl.class);

    private final MailSenderService mailSenderService;
    private final MailLogService mailLogService;
    private final MailRecipientService mailRecipientService;

    public MailServiceImpl(MailSenderService mailSenderService,
                           MailLogService mailLogService,
                           MailRecipientService mailRecipientService) {
        this.mailSenderService = mailSenderService;
        this.mailLogService = mailLogService;
        this.mailRecipientService = mailRecipientService;
    }

    @Override
    public void sendAndLogMail(String to, String subject, String content, String messageId,
                                MailType mailType, boolean isHtml) {
        MailAccount mailAccount = mailSenderService.getMailAccount();
        MailLog mailLog = MailLogBuilder.build(messageId, to, subject, content, mailType);
        try {
            MailUtil.send(mailAccount, to, subject, content, isHtml);
            mailLog.setSendStatus("SUCCESS");
        } catch (Exception e) {
            mailLog.setSendStatus("FAILED");
            mailLog.setFailReason(e.getMessage());
            mailLog.setRetryCount(0);
            throw new RuntimeException("邮件发送失败", e);
        } finally {
            mailLogService.saveOrUpdateByMessageId(mailLog);
        }
    }

    @Override
    public void sendByMailType(MailType mailType, String subject, String content, boolean isHtml) {
        List<MailRecipient> recipients = mailRecipientService.listByMailType(mailType.getCode());
        if (recipients.isEmpty()) {
            log.warn("没有启用且订阅了 [{}] 类型邮件的收件人，跳过发送", mailType.getCode());
            return;
        }

        log.info("开始按类型 [{}] 群发邮件，共 {} 个收件人", mailType.getCode(), recipients.size());
        for (MailRecipient recipient : recipients) {
            try {
                sendAndLogMail(
                        recipient.getEmail(),
                        subject,
                        content,
                        IdUtil.simpleUUID(),
                        mailType,
                        isHtml
                );
            } catch (Exception e) {
                log.error("发送邮件到 {} 失败: {}", recipient.getEmail(), e.getMessage());
                // 不中断，继续发下一个
            }
        }
        log.info("按类型 [{}] 群发邮件完成", mailType.getCode());
    }
}
