package com.mok.framework.mail.service.impl;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.mok.framework.mail.service.MailTransport;
import org.springframework.stereotype.Service;

@Service
public class HutoolMailTransport implements MailTransport {

    @Override
    public void send(MailAccount account, String recipient, String subject, String content, boolean html) {
        MailUtil.send(account, recipient, subject, content, html);
    }
}
