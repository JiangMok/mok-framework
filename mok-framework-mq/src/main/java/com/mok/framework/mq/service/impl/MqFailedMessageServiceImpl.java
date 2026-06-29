package com.mok.framework.mq.service.impl;

import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.mq.mapper.MqFailedMessageMapper;
import com.mok.framework.mq.service.MqFailedMessageService;
import org.springframework.stereotype.Service;

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
}
