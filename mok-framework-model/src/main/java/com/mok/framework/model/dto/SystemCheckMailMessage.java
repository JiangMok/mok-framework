package com.mok.framework.model.dto;

import java.util.Objects;

/**
 * 系统检查邮件推送的消息实体
 */
public class SystemCheckMailMessage {

    // 消息ID
    private String id;
    // 收件人
    private String recipient;
    // 邮件主题
    private String subject;
    // 邮件正文
    private String content;
    // 是否为 HTML 邮件
    private boolean isHtml;

    @Override
    public String toString() {
        return "SystemCheckMailMessage{" +
                "id='" + id + '\'' +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SystemCheckMailMessage that = (SystemCheckMailMessage) o;
        return Objects.equals(id, that.id) && Objects.equals(recipient, that.recipient) && Objects.equals(subject, that.subject) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recipient, subject, content);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean html) {
        isHtml = html;
    }
}
