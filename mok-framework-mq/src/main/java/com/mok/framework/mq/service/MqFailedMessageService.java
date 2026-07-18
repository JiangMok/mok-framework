package com.mok.framework.mq.service;

import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
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

    /**
     * @description: 分页查询MQ失败消息
     * @author: mok
     * @date: 2026/7/18
     * @param: [param]
     * @return: com.mok.framework.common.PageResult<com.mok.framework.model.entity.MqFailedMessage>
    **/
    PageResult<MqFailedMessage> getPage(PageParam param);

    /**
     * @description: 根据ID查询MQ失败消息
     * @author: mok
     * @date: 2026/7/18
     * @param: [id]
     * @return: com.mok.framework.model.entity.MqFailedMessage
    **/
    MqFailedMessage getById(String id);

    /**
     * @description: 根据ID删除MQ失败消息
     * @author: mok
     * @date: 2026/7/18
     * @param: [id]
     * @return: void
    **/
    void deleteById(String id);

    /**
     * @description: 标记MQ失败消息为已处理
     * @author: mok
     * @date: 2026/7/18
     * @param: [id, resolvedBy, remark]
     * @return: void
    **/
    void resolve(String id, String resolvedBy, String remark);
}
