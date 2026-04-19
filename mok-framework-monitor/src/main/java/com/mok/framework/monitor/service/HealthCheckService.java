package com.mok.framework.monitor.service;

import java.util.Map;

/**
 * @description: 健康检查service
 * @author: JN
 * @date: 2026/1/6 14:54
 * @param:
 * @return:
 **/
public interface HealthCheckService {
    /**
     * @description: 获取系统健康信息
     * @author: JN
     * @date: 2026/1/6 14:54
     * @param: []
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    Map<String, Object> performHealthCheck();
}
