package com.mok.baseframe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @description: 用户 DTO(数据传输对象)
 * @author: JN
 * @date: 2026/1/2
 */
public class UserDTO implements Serializable  {

    private static final long serialVersionUID = 1L;

    private String id;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    private Integer status = 1;

    private List<String> roleIds;

    // 默认构造函数
    public UserDTO() {
    }

    // 全参数构造函数（可选）
    public UserDTO(String id, String nickname, String password, String username,
                   String phone, String email, String avatar, Integer status, List<String> roleIds) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.status = status != null ? status : 1;
        this.roleIds = roleIds;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status != null ? status : 1;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
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
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(id, userDTO.id) &&
                Objects.equals(nickname, userDTO.nickname) &&
                Objects.equals(password, userDTO.password) &&
                Objects.equals(username, userDTO.username) &&
                Objects.equals(phone, userDTO.phone) &&
                Objects.equals(email, userDTO.email) &&
                Objects.equals(avatar, userDTO.avatar) &&
                Objects.equals(status, userDTO.status) &&
                Objects.equals(roleIds, userDTO.roleIds);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, password, username, phone, email, avatar, status, roleIds);
    }

    // toString 方法（出于安全考虑，不输出密码）
    @Override
    public String toString() {
        return "UserDTO{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='[PROTECTED]'" +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status=" + status +
                ", roleIds=" + roleIds +
                '}';
    }
}