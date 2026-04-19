package com.mok.framework.model.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 操作日志消息DTO
 * 独立于core模块，避免依赖
 */
public class OperationLogMessage {
    
    private String id;
    private String title;
    private String businessType;
    private String method;
    private String requestMethod;
    private String operUrl;
    private String operIp;
    private String operatorName;
    private Integer operatorType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getOperUrl() {
        return operUrl;
    }

    public void setOperUrl(String operUrl) {
        this.operUrl = operUrl;
    }

    public String getOperIp() {
        return operIp;
    }

    public void setOperIp(String operIp) {
        this.operIp = operIp;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(Integer operatorType) {
        this.operatorType = operatorType;
    }

    public String getOperParam() {
        return operParam;
    }

    public void setOperParam(String operParam) {
        this.operParam = operParam;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public void setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
    }

    private String operParam;
    private String jsonResult;
    private Integer status;
    private String errorMsg;
    private LocalDateTime operTime;
    
    // 构造方法、getter、setter...

    @Override
    public String toString() {
        return "OperationLogMessage{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", businessType='" + businessType + '\'' +
                ", method='" + method + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", operUrl='" + operUrl + '\'' +
                ", operIp='" + operIp + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", operatorType=" + operatorType +
                ", operParam='" + operParam + '\'' +
                ", jsonResult='" + jsonResult + '\'' +
                ", status=" + status +
                ", errorMsg='" + errorMsg + '\'' +
                ", operTime=" + operTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperationLogMessage that = (OperationLogMessage) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(businessType, that.businessType) && Objects.equals(method, that.method) && Objects.equals(requestMethod, that.requestMethod) && Objects.equals(operUrl, that.operUrl) && Objects.equals(operIp, that.operIp) && Objects.equals(operatorName, that.operatorName) && Objects.equals(operatorType, that.operatorType) && Objects.equals(operParam, that.operParam) && Objects.equals(jsonResult, that.jsonResult) && Objects.equals(status, that.status) && Objects.equals(errorMsg, that.errorMsg) && Objects.equals(operTime, that.operTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, businessType, method, requestMethod, operUrl, operIp, operatorName, operatorType, operParam, jsonResult, status, errorMsg, operTime);
    }
}