package com.mok.framework.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Objects;

/**
 * 收件人-邮件类型关联实体
 * 对应数据库表 sys_mail_recipient_type
 *
 * @author mok
 * @date 2026/7/17
 */
@TableName("sys_mail_recipient_type")
public class MailRecipientType {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("recipient_id")
    private String recipientId;

    @TableField("mail_type")
    private String mailType;

    public MailRecipientType() {
    }

    public MailRecipientType(String id, String recipientId, String mailType) {
        this.id = id;
        this.recipientId = recipientId;
        this.mailType = mailType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String recipientId;
        private String mailType;

        public Builder id(String id) { this.id = id; return this; }
        public Builder recipientId(String recipientId) { this.recipientId = recipientId; return this; }
        public Builder mailType(String mailType) { this.mailType = mailType; return this; }

        public MailRecipientType build() {
            return new MailRecipientType(id, recipientId, mailType);
        }
    }

    // ========== Getter / Setter（链式） ==========
    public String getId() { return id; }
    public MailRecipientType setId(String id) { this.id = id; return this; }

    public String getRecipientId() { return recipientId; }
    public MailRecipientType setRecipientId(String recipientId) { this.recipientId = recipientId; return this; }

    public String getMailType() { return mailType; }
    public MailRecipientType setMailType(String mailType) { this.mailType = mailType; return this; }

    // ========== equals / hashCode / toString ==========
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailRecipientType that = (MailRecipientType) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(recipientId, that.recipientId) &&
                Objects.equals(mailType, that.mailType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recipientId, mailType);
    }

    @Override
    public String toString() {
        return "MailRecipientType{" +
                "id='" + id + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", mailType='" + mailType + '\'' +
                '}';
    }
}
