package com.mok.framework.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统发件箱配置实体
 * 全局唯一一条记录，对应数据库表 sys_mail_sender
 *
 * @author mok
 * @date 2026/7/17
 */
@TableName("sys_mail_sender")
public class MailSender implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("ssl_enable")
    private Integer sslEnable;

    @TableField("from_address")
    private String fromAddress;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("status")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public MailSender() {
    }

    public MailSender(String id, String host, Integer port, Integer sslEnable,
                      String fromAddress, String username, String password,
                      Integer status, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.sslEnable = sslEnable;
        this.fromAddress = fromAddress;
        this.username = username;
        this.password = password;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String host;
        private Integer port;
        private Integer sslEnable;
        private String fromAddress;
        private String username;
        private String password;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public Builder id(String id) { this.id = id; return this; }
        public Builder host(String host) { this.host = host; return this; }
        public Builder port(Integer port) { this.port = port; return this; }
        public Builder sslEnable(Integer sslEnable) { this.sslEnable = sslEnable; return this; }
        public Builder fromAddress(String fromAddress) { this.fromAddress = fromAddress; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder createTime(LocalDateTime createTime) { this.createTime = createTime; return this; }
        public Builder updateTime(LocalDateTime updateTime) { this.updateTime = updateTime; return this; }

        public MailSender build() {
            return new MailSender(id, host, port, sslEnable, fromAddress, username,
                    password, status, createTime, updateTime);
        }
    }

    // ========== Getter / Setter（链式） ==========
    public String getId() { return id; }
    public MailSender setId(String id) { this.id = id; return this; }

    public String getHost() { return host; }
    public MailSender setHost(String host) { this.host = host; return this; }

    public Integer getPort() { return port; }
    public MailSender setPort(Integer port) { this.port = port; return this; }

    public Integer getSslEnable() { return sslEnable; }
    public MailSender setSslEnable(Integer sslEnable) { this.sslEnable = sslEnable; return this; }

    public String getFromAddress() { return fromAddress; }
    public MailSender setFromAddress(String fromAddress) { this.fromAddress = fromAddress; return this; }

    public String getUsername() { return username; }
    public MailSender setUsername(String username) { this.username = username; return this; }

    public String getPassword() { return password; }
    public MailSender setPassword(String password) { this.password = password; return this; }

    public Integer getStatus() { return status; }
    public MailSender setStatus(Integer status) { this.status = status; return this; }

    public LocalDateTime getCreateTime() { return createTime; }
    public MailSender setCreateTime(LocalDateTime createTime) { this.createTime = createTime; return this; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public MailSender setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; return this; }

    // ========== equals / hashCode / toString ==========
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailSender that = (MailSender) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(host, that.host) &&
                Objects.equals(port, that.port) &&
                Objects.equals(sslEnable, that.sslEnable) &&
                Objects.equals(fromAddress, that.fromAddress) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, port, sslEnable, fromAddress, username,
                password, status, createTime, updateTime);
    }

    @Override
    public String toString() {
        return "MailSender{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", sslEnable=" + sslEnable +
                ", fromAddress='" + fromAddress + '\'' +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
