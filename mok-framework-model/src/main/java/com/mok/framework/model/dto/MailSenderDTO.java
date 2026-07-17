package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * 发件箱配置 DTO
 *
 * @author mok
 * @date 2026/7/17
 */
public class MailSenderDTO {

    private String id;

    @NotBlank(message = "SMTP 服务器地址不能为空")
    private String host;

    @NotNull(message = "端口不能为空")
    private Integer port;

    @NotNull(message = "SSL 启用状态不能为空")
    private Integer sslEnable;

    @NotBlank(message = "发件人地址不能为空")
    private String fromAddress;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;

    public MailSenderDTO() {
    }

    public MailSenderDTO(String id, String host, Integer port, Integer sslEnable,
                         String fromAddress, String username, String password, Integer status) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.sslEnable = sslEnable;
        this.fromAddress = fromAddress;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    // ========== Getter / Setter ==========
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public Integer getSslEnable() { return sslEnable; }
    public void setSslEnable(Integer sslEnable) { this.sslEnable = sslEnable; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailSenderDTO that = (MailSenderDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(host, that.host) &&
                Objects.equals(port, that.port) &&
                Objects.equals(sslEnable, that.sslEnable) &&
                Objects.equals(fromAddress, that.fromAddress) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, port, sslEnable, fromAddress, username, password, status);
    }

    @Override
    public String toString() {
        return "MailSenderDTO{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", sslEnable=" + sslEnable +
                ", fromAddress='" + fromAddress + '\'' +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", status=" + status +
                '}';
    }
}
