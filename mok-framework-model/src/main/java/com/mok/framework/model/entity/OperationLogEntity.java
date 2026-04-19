package com.mok.framework.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @description: 操作日志实体类（Elasticsearch 版本）
 * @author: JN
 * @date: 2026/1/5 11:16
 **/
@Document(indexName = "operation_log")   // 索引名，全小写
public class OperationLogEntity {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)  // 如需中文分词可配置，否则使用 standard
    private String title;

    @Field(type = FieldType.Keyword)      // 业务类型，精确匹配
    private String businessType;

    @Field(type = FieldType.Keyword)
    private String method;

    @Field(type = FieldType.Keyword)
    private String requestMethod;

    @Field(type = FieldType.Integer)
    private Integer operatorType;

    @Field(type = FieldType.Keyword)
    private String operatorId;

    @Field(type = FieldType.Text)
    private String operatorName;

    @Field(type = FieldType.Text)
    private String deptName;

    @Field(type = FieldType.Text)
    private String operUrl;

    @Field(type = FieldType.Keyword)
    private String operIp;

    @Field(type = FieldType.Text)
    private String operLocation;

    @Field(type = FieldType.Text)
    private String operParam;

    @Field(type = FieldType.Text)
    private String jsonResult;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Text)
    private String errorMsg;

    @Field(type = FieldType.Date, format = DateFormat.strict_date_hour_minute_second)
    private LocalDateTime operTime;

    @Field(type = FieldType.Date, format = DateFormat.strict_date_hour_minute_second)
    private LocalDateTime createTime;

    // 构造方法
    public OperationLogEntity() {
    }

    public OperationLogEntity(String id, String title, String businessType, String method,
                              String requestMethod, Integer operatorType, String operatorId,
                              String operatorName, String deptName, String operUrl,
                              String operIp, String operLocation, String operParam,
                              String jsonResult, Integer status, String errorMsg,
                              LocalDateTime operTime, LocalDateTime createTime) {
        this.id = id;
        this.title = title;
        this.businessType = businessType;
        this.method = method;
        this.requestMethod = requestMethod;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.deptName = deptName;
        this.operUrl = operUrl;
        this.operIp = operIp;
        this.operLocation = operLocation;
        this.operParam = operParam;
        this.jsonResult = jsonResult;
        this.status = status;
        this.errorMsg = errorMsg;
        this.operTime = operTime;
        this.createTime = createTime;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public OperationLogEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public OperationLogEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBusinessType() {
        return businessType;
    }

    public OperationLogEntity setBusinessType(String businessType) {
        this.businessType = businessType;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public OperationLogEntity setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public OperationLogEntity setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public OperationLogEntity setOperatorType(Integer operatorType) {
        this.operatorType = operatorType;
        return this;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public OperationLogEntity setOperatorId(String operatorId) {
        this.operatorId = operatorId;
        return this;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public OperationLogEntity setOperatorName(String operatorName) {
        this.operatorName = operatorName;
        return this;
    }

    public String getDeptName() {
        return deptName;
    }

    public OperationLogEntity setDeptName(String deptName) {
        this.deptName = deptName;
        return this;
    }

    public String getOperUrl() {
        return operUrl;
    }

    public OperationLogEntity setOperUrl(String operUrl) {
        this.operUrl = operUrl;
        return this;
    }

    public String getOperIp() {
        return operIp;
    }

    public OperationLogEntity setOperIp(String operIp) {
        this.operIp = operIp;
        return this;
    }

    public String getOperLocation() {
        return operLocation;
    }

    public OperationLogEntity setOperLocation(String operLocation) {
        this.operLocation = operLocation;
        return this;
    }

    public String getOperParam() {
        return operParam;
    }

    public OperationLogEntity setOperParam(String operParam) {
        this.operParam = operParam;
        return this;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public OperationLogEntity setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public OperationLogEntity setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public OperationLogEntity setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public OperationLogEntity setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public OperationLogEntity setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    // equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperationLogEntity that = (OperationLogEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(businessType, that.businessType) &&
                Objects.equals(method, that.method) &&
                Objects.equals(requestMethod, that.requestMethod) &&
                Objects.equals(operatorType, that.operatorType) &&
                Objects.equals(operatorId, that.operatorId) &&
                Objects.equals(operatorName, that.operatorName) &&
                Objects.equals(deptName, that.deptName) &&
                Objects.equals(operUrl, that.operUrl) &&
                Objects.equals(operIp, that.operIp) &&
                Objects.equals(operLocation, that.operLocation) &&
                Objects.equals(operParam, that.operParam) &&
                Objects.equals(jsonResult, that.jsonResult) &&
                Objects.equals(status, that.status) &&
                Objects.equals(errorMsg, that.errorMsg) &&
                Objects.equals(operTime, that.operTime) &&
                Objects.equals(createTime, that.createTime);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id, title, businessType, method, requestMethod, operatorType, operatorId, operatorName,
                deptName, operUrl, operIp, operLocation, operParam, jsonResult, status, errorMsg, operTime, createTime);
    }

    // toString 方法
    @Override
    public String toString() {
        return "OperationLogEntity{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", businessType='" + businessType + '\'' +
                ", method='" + method + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", operatorType=" + operatorType +
                ", operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", deptName='" + deptName + '\'' +
                ", operUrl='" + operUrl + '\'' +
                ", operIp='" + operIp + '\'' +
                ", operLocation='" + operLocation + '\'' +
                ", operParam='" + operParam + '\'' +
                ", jsonResult='" + jsonResult + '\'' +
                ", status=" + status +
                ", errorMsg='" + errorMsg + '\'' +
                ", operTime=" + operTime +
                ", createTime=" + createTime +
                '}';
    }

    public static class Builder {
        private String id;
        private String title;
        private String businessType;
        private String method;
        private String requestMethod;
        private Integer operatorType;
        private String operatorId;
        private String operatorName;
        private String deptName;
        private String operUrl;
        private String operIp;
        private String operLocation;
        private String operParam;
        private String jsonResult;
        private Integer status;
        private String errorMsg;
        private LocalDateTime operTime;
        private LocalDateTime createTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder businessType(String businessType) {
            this.businessType = businessType;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder requestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder operatorType(Integer operatorType) {
            this.operatorType = operatorType;
            return this;
        }

        public Builder operatorId(String operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public Builder operatorName(String operatorName) {
            this.operatorName = operatorName;
            return this;
        }

        public Builder deptName(String deptName) {
            this.deptName = deptName;
            return this;
        }

        public Builder operUrl(String operUrl) {
            this.operUrl = operUrl;
            return this;
        }

        public Builder operIp(String operIp) {
            this.operIp = operIp;
            return this;
        }

        public Builder operLocation(String operLocation) {
            this.operLocation = operLocation;
            return this;
        }

        public Builder operParam(String operParam) {
            this.operParam = operParam;
            return this;
        }

        public Builder jsonResult(String jsonResult) {
            this.jsonResult = jsonResult;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder errorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
            return this;
        }

        public Builder operTime(LocalDateTime operTime) {
            this.operTime = operTime;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public OperationLogEntity build() {
            return new OperationLogEntity(id, title, businessType, method, requestMethod,
                    operatorType, operatorId, operatorName, deptName, operUrl, operIp,
                    operLocation, operParam, jsonResult, status, errorMsg, operTime, createTime);
        }
    }
}