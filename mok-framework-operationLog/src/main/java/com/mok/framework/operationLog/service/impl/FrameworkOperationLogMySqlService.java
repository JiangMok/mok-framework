package com.mok.framework.operationLog.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import top.jiangmok.operationlog.config.OperationLogProperties;
import top.jiangmok.operationlog.service.impl.OperationLogMySqlServiceImpl;

/**
 * 将 Starter 的 MySQL 存储路由到脚手架独立操作日志数据源。
 *
 * @author mok
 */
@Service
@DS("operationLog")
@ConditionalOnExpression(
        "'${mok.operation-log.enabled:true}' == 'true' " +
        "and '${mok.operation-log.save-location:file}' == 'mysql'")
public class FrameworkOperationLogMySqlService extends OperationLogMySqlServiceImpl {

    public FrameworkOperationLogMySqlService(OperationLogProperties properties) {
        super(properties);
    }
}
