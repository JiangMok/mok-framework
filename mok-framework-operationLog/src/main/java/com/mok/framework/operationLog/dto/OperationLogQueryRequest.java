package com.mok.framework.operationLog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志分页查询参数。
 *
 * @author mok
 */
public class OperationLogQueryRequest {

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小不能小于 1")
    @Max(value = 1000, message = "每页大小不能超过 1000")
    private Integer pageSize = 10;

    private String keyword;
    private Integer status;
    private String businessType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public Map<String, Object> toConditions() {
        Map<String, Object> conditions = new HashMap<>();
        putIfPresent(conditions, "status", status);
        putIfPresent(conditions, "businessType", businessType);
        putIfPresent(conditions, "startTime", startTime);
        putIfPresent(conditions, "endTime", endTime);
        return conditions;
    }

    private void putIfPresent(Map<String, Object> conditions, String key, Object value) {
        if (value instanceof String text && text.isBlank()) {
            return;
        }
        if (value != null) {
            conditions.put(key, value);
        }
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}

