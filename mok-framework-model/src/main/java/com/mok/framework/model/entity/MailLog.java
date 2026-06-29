package com.mok.framework.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统检查邮件发送记录实体
 * 对应数据库表 system_check_mail_log
 */
public class MailLog {

    private String id;
    private String messageId;
    private String recipient;
    private String subject;
    private String mailType;          // 邮件类型：ALERT / NOTIFICATION / SYSTEM_CHECK ...
    private String content;
    private String sendStatus;        // SUCCESS / FAILED
    private String failReason;
    private LocalDateTime sendTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 重试次数
     */
    private Integer retryCount;

    public MailLog() {}

    // 全参构造器（按需添加）

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MailLog mailLog = (MailLog) o;
        return Objects.equals(id, mailLog.id) && Objects.equals(messageId, mailLog.messageId) && Objects.equals(recipient, mailLog.recipient) && Objects.equals(subject, mailLog.subject) && Objects.equals(mailType, mailLog.mailType) && Objects.equals(content, mailLog.content) && Objects.equals(sendStatus, mailLog.sendStatus) && Objects.equals(failReason, mailLog.failReason) && Objects.equals(sendTime, mailLog.sendTime) && Objects.equals(createTime, mailLog.createTime) && Objects.equals(retryCount, mailLog.retryCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, messageId, recipient, subject, mailType, content, sendStatus, failReason, sendTime, createTime, retryCount);
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    // ========== Getter & Setter ==========
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMailType() { return mailType; }
    public void setMailType(String mailType) { this.mailType = mailType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSendStatus() { return sendStatus; }
    public void setSendStatus(String sendStatus) { this.sendStatus = sendStatus; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "MailLog{" +
                "id=" + id +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", mailType='" + mailType + '\'' +
                ", sendStatus='" + sendStatus + '\'' +
                '}';
    }
}