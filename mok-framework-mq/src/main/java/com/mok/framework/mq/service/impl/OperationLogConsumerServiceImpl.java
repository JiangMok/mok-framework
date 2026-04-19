package com.mok.framework.mq.service;

@Service
public class OperationLogConsumerServiceImpl implements OperationLogConsumerService{
    @Override
    public boolean checkOperationLogExistsById(String id) {
        return false;
    }
}
