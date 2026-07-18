package com.mok.framework.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.mq.mapper.MqFailedMessageMapper;
import com.mok.framework.mq.service.MqFailedMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        mqFailedMessageMapper.insert(mqFailedMessage);
    }

    @Override
    public PageResult<MqFailedMessage> getPage(PageParam param) {
        Page<MqFailedMessage> page = new Page<>(param.getPageNum(), param.getPageSize());
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
        return mqFailedMessageMapper.selectById(id);
    }

    @Override
    public void deleteById(String id) {
        mqFailedMessageMapper.deleteById(id);
    }

    @Override
    public void resolve(String id, String resolvedBy, String remark) {
        MqFailedMessage message = mqFailedMessageMapper.selectById(id);
        if (message != null) {
            message.setStatus("RESOLVED");
            message.setResolvedBy(resolvedBy);
            message.setResolvedTime(LocalDateTime.now());
            message.setRemark(remark);
            mqFailedMessageMapper.updateById(message);
        }
    }
}
