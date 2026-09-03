package com.mok.framework.mail.util;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * 邮件消息幂等 ID 生成器。
 */
public final class MailMessageIdGenerator {

    private MailMessageIdGenerator() {
    }

    /**
     * 同一业务事件、同一收件人始终生成相同的消息 ID。
     */
    public static String generate(String eventId, String recipient) {
        if (!StringUtils.hasText(eventId)) {
            throw new IllegalArgumentException("邮件事件ID不能为空");
        }
        if (!StringUtils.hasText(recipient)) {
            throw new IllegalArgumentException("邮件收件人不能为空");
        }
        String normalizedRecipient = recipient.trim().toLowerCase(Locale.ROOT);
        String source = eventId.trim() + '|' + normalizedRecipient;
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "");
    }
}
