package com.mok.framework.mq.util;

import com.mok.framework.model.entity.MqFailedMessage;

/**
 * 从消息体原始字节中提取自定义字段，填充到 MqFailedMessage 中
 */
@FunctionalInterface
public interface MessageFieldExtractor {
    void extract(byte[] body, MqFailedMessage record);
}