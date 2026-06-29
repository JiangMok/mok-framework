package com.mok.framework.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MQ死信失败记录实体
 * 对应数据库表 mq_failed_message
 */
public class MqFailedMessage {

    private String id;
    private String messageId;
    private String messageType;          // 消息类型
    private String messageBody;          // 失败消息完整内容（JSON）
    private String originalQueue;        // 原始队列
    private String deadQueue;            // 死信队列
    private String dlxExchange;          // 死信交换机
    private String dlxRoutingKey;        // 死信路由键
    private String failReason;           // 失败原因
    private String xDeathHeader;         // x-death 头信息
    private Integer retryCount;          // 已重试次数
    private Integer maxRetry;            // 最大重试次数
    private String status;               // 处理状态
    private LocalDateTime originalTimestamp; // 原始消息产生时间
    private LocalDateTime failedTime;    // 进入失败表时间
    private String resolvedBy;           // 处理人
    private LocalDateTime resolvedTime;  // 处理时间
    private String remark;               // 备注

    public MqFailedMessage() {
    }

    // 全参构造器（按需使用）
    public MqFailedMessage(String id, String messageId, String messageType, String messageBody,
                           String originalQueue, String deadQueue, String dlxExchange,
                           String dlxRoutingKey, String failReason, String xDeathHeader,
                           Integer retryCount, Integer maxRetry, String status,
                           LocalDateTime originalTimestamp, LocalDateTime failedTime,
                           String resolvedBy, LocalDateTime resolvedTime, String remark) {
        this.id = id;
        this.messageId = messageId;
        this.messageType = messageType;
        this.messageBody = messageBody;
        this.originalQueue = originalQueue;
        this.deadQueue = deadQueue;
        this.dlxExchange = dlxExchange;
        this.dlxRoutingKey = dlxRoutingKey;
        this.failReason = failReason;
        this.xDeathHeader = xDeathHeader;
        this.retryCount = retryCount;
        this.maxRetry = maxRetry;
        this.status = status;
        this.originalTimestamp = originalTimestamp;
        this.failedTime = failedTime;
        this.resolvedBy = resolvedBy;
        this.resolvedTime = resolvedTime;
        this.remark = remark;
    }

    // ---- Getter 和 Setter ----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }

    public String getOriginalQueue() { return originalQueue; }
    public void setOriginalQueue(String originalQueue) { this.originalQueue = originalQueue; }

    public String getDeadQueue() { return deadQueue; }
    public void setDeadQueue(String deadQueue) { this.deadQueue = deadQueue; }

    public String getDlxExchange() { return dlxExchange; }
    public void setDlxExchange(String dlxExchange) { this.dlxExchange = dlxExchange; }

    public String getDlxRoutingKey() { return dlxRoutingKey; }
    public void setDlxRoutingKey(String dlxRoutingKey) { this.dlxRoutingKey = dlxRoutingKey; }

    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }

    public String getXDeathHeader() { return xDeathHeader; }
    public void setXDeathHeader(String xDeathHeader) { this.xDeathHeader = xDeathHeader; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Integer getMaxRetry() { return maxRetry; }
    public void setMaxRetry(Integer maxRetry) { this.maxRetry = maxRetry; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public LocalDateTime getFailedTime() { return failedTime; }
    public void setFailedTime(LocalDateTime failedTime) { this.failedTime = failedTime; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedTime() { return resolvedTime; }
    public void setResolvedTime(LocalDateTime resolvedTime) { this.resolvedTime = resolvedTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqFailedMessage that = (MqFailedMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MqFailedMessage{" +
                "id=" + id +
                ", messageId='" + messageId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", originalQueue='" + originalQueue + '\'' +
                ", failReason='" + failReason + '\'' +
                ", status='" + status + '\'' +
                ", failedTime=" + failedTime +
                '}';
    }
}