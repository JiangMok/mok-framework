package com.mok.framework.mail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.common.BusinessException;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.mail.mapper.MailLogMapper;
import com.mok.framework.mail.service.MailDeliveryClaim;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.model.entity.MailLog;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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
        if (mailLogMapper.insert(mailLog) != 1) {
            throw new BusinessException("邮件日志保存失败");
        }
    }

    @Override
    public MailLog getMailLogByMessageId(String messageId) {
        return mailLogMapper.selectOne(
                new LambdaQueryWrapper<MailLog>().eq(MailLog::getMessageId, messageId)
        );
    }

    @Override
    public void updateById(MailLog mailLog) {
        if (mailLogMapper.updateById(mailLog) != 1) {
            throw new BusinessException("邮件日志更新失败");
        }
    }

    @Override
    public PageResult<MailLog> getPage(PageParam param) {
        Page<MailLog> page = param.toPageWithoutOrder();
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
        MailLog mailLog = mailLogMapper.selectById(id);
        if (mailLog == null) {
            throw new BusinessException("邮件日志不存在");
        }
        return mailLog;
    }

    @Override
    public void deleteById(String id) {
        if (mailLogMapper.deleteById(id) != 1) {
            throw new BusinessException("邮件日志不存在");
        }
    }

    @Override
    public MailDeliveryClaim claimDelivery(MailLog mailLog, long sendingLeaseSeconds) {
        if (sendingLeaseSeconds <= 0) {
            throw new IllegalArgumentException("邮件投递租约必须大于0");
        }
        mailLog.setSendStatus("SENDING");
        mailLog.setFailReason(null);
        mailLog.setRetryCount(0);
        try {
            if (mailLogMapper.insert(mailLog) != 1) {
                throw new BusinessException("邮件投递占位保存失败");
            }
            return MailDeliveryClaim.CLAIMED;
        } catch (DuplicateKeyException duplicateKeyException) {
            MailLog existingLog = getMailLogByMessageId(mailLog.getMessageId());
            if (existingLog == null) {
                throw duplicateKeyException;
            }
            if ("SUCCESS".equals(existingLog.getSendStatus())) {
                return MailDeliveryClaim.ALREADY_SUCCESS;
            }

            int affectedRows = mailLogMapper.claimForDelivery(
                    mailLog.getMessageId(), mailLog.getSendTime(), sendingLeaseSeconds);
            if (affectedRows == 1) {
                return MailDeliveryClaim.CLAIMED;
            }

            existingLog = getMailLogByMessageId(mailLog.getMessageId());
            return existingLog != null && "SUCCESS".equals(existingLog.getSendStatus())
                    ? MailDeliveryClaim.ALREADY_SUCCESS
                    : MailDeliveryClaim.IN_PROGRESS;
        }
    }

    @Override
    public boolean completeDelivery(String messageId, LocalDateTime claimTime,
                                    String sendStatus, String failReason) {
        return mailLogMapper.completeDelivery(
                messageId, claimTime, sendStatus, failReason) == 1;
    }
}
