package com.mok.framework.mail.service.impl;

import com.mok.framework.mail.service.MailRecipientService;
import com.mok.framework.mail.service.MailSenderService;
import com.mok.framework.model.entity.MailRecipient;
import com.mok.framework.model.enums.MailType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MailServiceImplTest {

    @Test
    void directRetryReusesRecipientMessageIdsForTheSameAlert() {
        MailSenderService senderService = mock(MailSenderService.class);
        MailRecipientService recipientService = mock(MailRecipientService.class);
        MailDeliveryExecutor deliveryExecutor = mock(MailDeliveryExecutor.class);
        when(recipientService.listByMailType(MailType.SYSTEM_CHECK.getCode()))
                .thenReturn(List.of(recipient("first@example.com"), recipient("second@example.com")));
        List<String> messageIds = new ArrayList<>();
        doAnswer(invocation -> {
            messageIds.add(invocation.getArgument(4));
            return null;
        }).when(deliveryExecutor).sendAndLog(
                any(Supplier.class), anyString(), anyString(), anyString(),
                anyString(), any(MailType.class), anyBoolean());
        MailServiceImpl service = new MailServiceImpl(senderService, recipientService, deliveryExecutor);

        service.sendByMailType(MailType.SYSTEM_CHECK, "subject", "content", true, "alert-1");
        service.sendByMailType(MailType.SYSTEM_CHECK, "subject", "content", true, "alert-1");

        assertThat(messageIds).hasSize(4);
        assertThat(messageIds.subList(0, 2)).containsExactlyElementsOf(messageIds.subList(2, 4));
    }

    private MailRecipient recipient(String email) {
        MailRecipient recipient = new MailRecipient();
        recipient.setEmail(email);
        return recipient;
    }
}
