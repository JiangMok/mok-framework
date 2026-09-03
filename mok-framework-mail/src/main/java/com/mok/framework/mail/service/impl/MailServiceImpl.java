package com.mok.framework.mail.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailRecipientService;
import com.mok.framework.mail.service.MailSenderService;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.MailMessageIdGenerator;
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
    private final MailRecipientService mailRecipientService;
    private final MailDeliveryExecutor mailDeliveryExecutor;

    public MailServiceImpl(MailSenderService mailSenderService,
                           MailRecipientService mailRecipientService,
                           MailDeliveryExecutor mailDeliveryExecutor) {
        this.mailSenderService = mailSenderService;
        this.mailRecipientService = mailRecipientService;
        this.mailDeliveryExecutor = mailDeliveryExecutor;
    }

    @Override
    public void sendAndLogMail(String to, String subject, String content, String messageId,
                                MailType mailType, boolean isHtml) {
        mailDeliveryExecutor.sendAndLog(
                mailSenderService::getMailAccount,
                to, subject, content, messageId, mailType, isHtml);
    }

    @Override
    public int sendByMailType(MailType mailType, String subject, String content, boolean isHtml) {
        return sendByMailType(mailType, subject, content, isHtml, IdUtil.simpleUUID());
    }

    @Override
    public int sendByMailType(MailType mailType, String subject, String content,
                              boolean isHtml, String eventId) {
        List<MailRecipient> recipients = mailRecipientService.listByMailType(mailType.getCode());
        if (recipients.isEmpty()) {
            log.warn("没有启用且订阅了 [{}] 类型邮件的收件人，跳过发送", mailType.getCode());
            return 0;
        }

        log.info("开始按类型 [{}] 群发邮件，共 {} 个收件人", mailType.getCode(), recipients.size());
        RuntimeException firstFailure = null;
        int failedCount = 0;
        for (MailRecipient recipient : recipients) {
            try {
                String messageId = MailMessageIdGenerator.generate(eventId, recipient.getEmail());
                mailDeliveryExecutor.sendAndLog(
                        mailSenderService::getMailAccount,
                        recipient.getEmail(), subject, content, messageId, mailType, isHtml);
            } catch (Exception e) {
                log.error("发送邮件到 {} 失败: {}", recipient.getEmail(), e.getMessage());
                failedCount++;
                if (firstFailure == null) {
                    firstFailure = e instanceof RuntimeException runtimeException
                            ? runtimeException
                            : new RuntimeException(e);
                }
            }
        }
        if (failedCount > 0) {
            throw new RuntimeException(
                    String.format("%d/%d 封邮件发送失败", failedCount, recipients.size()),
                    firstFailure);
        }
        log.info("按类型 [{}] 群发邮件完成", mailType.getCode());
        return recipients.size();
    }
}
