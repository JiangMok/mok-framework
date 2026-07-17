package com.mok.framework.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * 收件人 DTO
 *
 * @author mok
 * @date 2026/7/17
 */
public class MailRecipientDTO {

    private String id;

    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "收件人名称不能为空")
    private String name;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;

    /** 订阅的邮件类型列表 */
    private List<String> mailTypes;

    public MailRecipientDTO() {
    }

    public MailRecipientDTO(String id, String email, String name, Integer status, List<String> mailTypes) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
        this.mailTypes = mailTypes;
    }

    // ========== Getter / Setter ==========
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public List<String> getMailTypes() { return mailTypes; }
    public void setMailTypes(List<String> mailTypes) { this.mailTypes = mailTypes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailRecipientDTO that = (MailRecipientDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(email, that.email) &&
                Objects.equals(name, that.name) &&
                Objects.equals(status, that.status) &&
                Objects.equals(mailTypes, that.mailTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, status, mailTypes);
    }

    @Override
    public String toString() {
        return "MailRecipientDTO{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", mailTypes=" + mailTypes +
                '}';
    }
}
