package com.mok.framework.mail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.mapper.MailLogMapper;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.model.entity.MailLog;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author: mok
 * @date: 2026/6/29
 */
@Service
public class MailLogServiceImpl implements MailLogService {

    private final Logger log = LogUtils.getLogger(MailLogServiceImpl.class);

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
        log.info("========= existingLog:{}",existingLog);
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

    @Override
    public PageResult<MailLog> getPage(PageParam param) {
        Page<MailLog> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<MailLog> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：收件人、主题
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.and(w -> w
                    .like(MailLog::getRecipient, param.getKeyword())
                    .or().like(MailLog::getSubject, param.getKeyword())
            );
        }

        // 发送状态筛选
        if (param.getParams() != null && param.getParams().get("sendStatus") != null
                && !"".equals(param.getParams().get("sendStatus"))) {
            wrapper.eq(MailLog::getSendStatus, param.getParams().get("sendStatus"));
        }

        // 邮件类型筛选
        if (param.getParams() != null && param.getParams().get("mailType") != null
                && !"".equals(param.getParams().get("mailType"))) {
            wrapper.eq(MailLog::getMailType, param.getParams().get("mailType"));
        }

        // 时间范围筛选
        if (param.getParams() != null && param.getParams().get("startTime") != null) {
            wrapper.ge(MailLog::getSendTime, param.getParams().get("startTime"));
        }
        if (param.getParams() != null && param.getParams().get("endTime") != null) {
            wrapper.le(MailLog::getSendTime, param.getParams().get("endTime"));
        }

        wrapper.orderByDesc(MailLog::getCreateTime);
        IPage<MailLog> result = mailLogMapper.selectPage(page, wrapper);
        return PageResult.fromIPage(result);
    }

    @Override
    public MailLog getById(String id) {
        return mailLogMapper.selectById(id);
    }

    @Override
    public void deleteById(String id) {
        mailLogMapper.deleteById(id);
    }
}
