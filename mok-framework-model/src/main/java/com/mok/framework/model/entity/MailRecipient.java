package com.mok.framework.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 收件人管理实体
 * 对应数据库表 sys_mail_recipient
 *
 * @author mok
 * @date 2026/7/17
 */
@TableName("sys_mail_recipient")
public class MailRecipient implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("email")
    private String email;

    @TableField("name")
    private String name;

    @TableField("status")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public MailRecipient() {
    }

    public MailRecipient(String id, String email, String name, Integer status,
                         LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String email;
        private String name;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public Builder id(String id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder createTime(LocalDateTime createTime) { this.createTime = createTime; return this; }
        public Builder updateTime(LocalDateTime updateTime) { this.updateTime = updateTime; return this; }

        public MailRecipient build() {
            return new MailRecipient(id, email, name, status, createTime, updateTime);
        }
    }

    // ========== Getter / Setter（链式） ==========
    public String getId() { return id; }
    public MailRecipient setId(String id) { this.id = id; return this; }

    public String getEmail() { return email; }
    public MailRecipient setEmail(String email) { this.email = email; return this; }

    public String getName() { return name; }
    public MailRecipient setName(String name) { this.name = name; return this; }

    public Integer getStatus() { return status; }
    public MailRecipient setStatus(Integer status) { this.status = status; return this; }

    public LocalDateTime getCreateTime() { return createTime; }
    public MailRecipient setCreateTime(LocalDateTime createTime) { this.createTime = createTime; return this; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public MailRecipient setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; return this; }

    // ========== equals / hashCode / toString ==========
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailRecipient that = (MailRecipient) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(email, that.email) &&
                Objects.equals(name, that.name) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, status, createTime, updateTime);
    }

    @Override
    public String toString() {
        return "MailRecipient{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
