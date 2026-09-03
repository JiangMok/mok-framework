package com.mok.framework.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.BusinessException;
import com.mok.framework.common.constant.ResponseCode;
import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.mq.mapper.MqFailedMessageMapper;
import com.mok.framework.mq.service.MqFailedMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

/**
 *
 * @author: mok
 * @date: 2026/6/29
 */
@Service
public class MqFailedMessageServiceImpl implements MqFailedMessageService {

    private final MqFailedMessageMapper mqFailedMessageMapper;

    public MqFailedMessageServiceImpl(MqFailedMessageMapper mqFailedMessageMapper){
        this.mqFailedMessageMapper = mqFailedMessageMapper;
    }

    @Override
    public void saveMqFailedMessage(MqFailedMessage mqFailedMessage) {
        if (mqFailedMessageMapper.selectById(mqFailedMessage.getId()) != null) {
            return;
        }
        try {
            if (mqFailedMessageMapper.insert(mqFailedMessage) != 1) {
                throw new BusinessException("MQ失败消息保存失败");
            }
        } catch (DuplicateKeyException exception) {
            // ACK 失败导致的重复投递按幂等成功处理。
            if (mqFailedMessageMapper.selectById(mqFailedMessage.getId()) == null) {
                throw exception;
            }
        }
    }

    @Override
    public PageResult<MqFailedMessage> getPage(PageParam param) {
        Page<MqFailedMessage> page = param.toPageWithoutOrder();
        LambdaQueryWrapper<MqFailedMessage> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：消息类型、原始队列、失败原因
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.and(w -> w
                    .like(MqFailedMessage::getMessageType, param.getKeyword())
                    .or().like(MqFailedMessage::getOriginalQueue, param.getKeyword())
                    .or().like(MqFailedMessage::getFailReason, param.getKeyword())
            );
        }

        // 处理状态筛选
        if (param.getParams() != null && param.getParams().get("status") != null
                && !"".equals(param.getParams().get("status"))) {
            wrapper.eq(MqFailedMessage::getStatus, param.getParams().get("status"));
        }

        // 时间范围筛选
        if (param.getParams() != null && param.getParams().get("startTime") != null) {
            wrapper.ge(MqFailedMessage::getFailedTime, param.getParams().get("startTime"));
        }
        if (param.getParams() != null && param.getParams().get("endTime") != null) {
            wrapper.le(MqFailedMessage::getFailedTime, param.getParams().get("endTime"));
        }

        wrapper.orderByDesc(MqFailedMessage::getFailedTime);
        IPage<MqFailedMessage> result = mqFailedMessageMapper.selectPage(page, wrapper);
        return PageResult.fromIPage(result);
    }

    @Override
    public MqFailedMessage getById(String id) {
        MqFailedMessage message = mqFailedMessageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "MQ失败消息不存在");
        }
        return message;
    }

    @Override
    public void deleteById(String id) {
        if (mqFailedMessageMapper.deleteById(id) == 0) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "MQ失败消息不存在");
        }
    }

    @Override
    public void resolve(String id, String resolvedBy, String remark) {
        MqFailedMessage message = mqFailedMessageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "MQ失败消息不存在");
        }
        message.setStatus("RESOLVED");
        message.setResolvedBy(resolvedBy);
        message.setResolvedTime(LocalDateTime.now());
        message.setRemark(remark);
        if (mqFailedMessageMapper.updateById(message) != 1) {
            throw new BusinessException("MQ失败消息处理状态更新失败");
        }
    }
}
