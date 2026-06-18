package com.mok.framework.operationLog.service;


import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.model.entity.OperationLogEntity;

import java.time.LocalDateTime;

/**
 * @description: ElasticSearch 操作日志service
 * @author: mok
 * @date: 2026/3/27 15:54
 **/
public interface OperationLogService {

    /**
     * @description: 查询所有数据
     * @author: mok
     * @date: 2026/3/27 15:57
     * @param: []
     * @return: java.lang.Iterable<com.mok.baseframe.es.entity.OperationLogEntity>
     **/
    PageResult<OperationLogEntity> getPageList(PageParam param);

    /**
     * @description: 保存操作日志
     * @author: mok
     * @date: 2026/3/27 15:59
     * @param: []
     * @return: com.mok.baseframe.es.entity.OperationLogEntity
     **/
    void saveOperationLog(OperationLogEntity operationLogEntity);

    /**
     * @description: 通过ID获取
     * @author: mok
     * @date: 2026/3/27 16:01
     * @param: [id]
     * @return: com.mok.baseframe.es.entity.OperationLogEntity
     **/
    OperationLogEntity findById(String id);

    /**
     * @description: 删除指定日期前的日志 
     * @author: mok
     * @date: 2026/3/29 13:58
     * @param: [dateTime]
     * @return: int
    **/
    int cleanLogsBefore(LocalDateTime dateTime);
    
    /**
     * @description: 根据ID删除
     * @author: mok
     * @date: 2026/3/29 15:21
     * @param: [id]
     * @return: void
    **/
    void deleteById(String id);

    /**
     * @description: 根据 操作日志id 查询操作日志是否存在
     * @author: JN
     * @date: 2026/1/22 19:52
     * @param: [id]
     * @return: boolean true:存在,false:不存在
     **/
    boolean checkOperationLogExistsById(String id);
}
