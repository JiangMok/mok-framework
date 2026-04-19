package com.mok.framework.mq.service;

/**
 * 操作日志消费者服务
 */
public interface OperationLogConsumerService {

    /**
     * @description: 根据 操作日志id 查询操作日志是否存在
     * @author: JN
     * @date: 2026/1/22 19:52
     * @param: [id]
     * @return: boolean true:存在,false:不存在
     **/
    boolean checkOperationLogExistsById(String id);
}
