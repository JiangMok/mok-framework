package com.mok.framework.mail.service.impl;

import com.mok.framework.mail.mapper.MailLogMapper;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.model.entity.MailLog;
import org.springframework.stereotype.Service;

/**
 *
 * @author: mok
 * @date: 2026/6/29
 */
@Service
public class MailLogServiceImpl implements MailLogService {

    private final MailLogMapper mailLogMapper;

    public MailLogServiceImpl(MailLogMapper mailLogMapper){
        this.mailLogMapper=mailLogMapper;
    }

    @Override
    public void saveMailLog(MailLog mailLog) {
        mailLogMapper.insert(mailLog);
    }

    @Override
    public MailLog getMailLogByMessageId(String messageId) {
        return null;
    }

    @Override
    public void updateById(MailLog mailLog) {
        mailLogMapper.updateById(mailLog);
    }
}
