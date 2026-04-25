package com.mok.framework.mq.service.impl;

import com.mok.framework.operationLog.service.OperationLogService;
import com.mok.framework.model.entity.OperationLogEntity;
import com.mok.framework.mq.service.OperationLogConsumerService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogConsumerServiceImpl implements OperationLogConsumerService {

    private final OperationLogService operationLogService;

    public OperationLogConsumerServiceImpl(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Override
    public boolean checkOperationLogExistsById(String id) {
        OperationLogEntity operationLogEntity = operationLogService.findById(id);
        return operationLogEntity != null;
    }
}
