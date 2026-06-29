package com.mok.framework.mq.service;

import com.mok.framework.model.entity.MqFailedMessage;

public interface MqFailedMessageService {

    /**
     * @description:  保存失败的消息队列信息
     * @author: mok
     * @date: 2026/6/29 11:09
     * @param: [mqFailedMessage]
     * @return: void
    **/
    void saveMqFailedMessage(MqFailedMessage mqFailedMessage);
}
