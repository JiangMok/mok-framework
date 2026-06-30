package com.mok.framework.mail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        return mailLogMapper.selectOne(
                new LambdaQueryWrapper<MailLog>().eq(MailLog::getMessageId, messageId)
        );
    }

    @Override
    public void updateById(MailLog mailLog) {
        mailLogMapper.updateById(mailLog);
    }

    @Override
    public void saveOrUpdateByMessageId(MailLog mailLog) {
        // 1. 根据 messageId 查询已存在的记录
        MailLog existingLog = mailLogMapper.selectOne(
                new LambdaQueryWrapper<MailLog>().eq(MailLog::getMessageId, mailLog.getMessageId())
        );
        // 2. 存在则更新状态、失败原因、重试次数等
        if (existingLog != null) {
            existingLog.setSendStatus(mailLog.getSendStatus());
            existingLog.setFailReason(mailLog.getFailReason());
            existingLog.setRetryCount(mailLog.getRetryCount());
            existingLog.setSendTime(mailLog.getSendTime());
            // 其他需要更新的字段...
            mailLogMapper.updateById(existingLog);
        } else {
            // 3. 不存在则插入
            mailLogMapper.insert(mailLog);
        }
    }
}
