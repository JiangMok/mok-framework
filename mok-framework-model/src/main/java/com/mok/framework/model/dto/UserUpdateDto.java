package com.mok.baseframe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * 用户更新数据传输对象
 *
 * @description: 用户更新DTO
 * @author: JN
 * @date: 2026/1/13
 */
public class UserUpdateDto {

    private String id;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;
    private String password;
    private String confirmPassword;
    private String targetUserId;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    private Integer status = 1;

    private List<String> roleIds;

    /**
     * 无参构造函数
     */
    public UserUpdateDto() {
    }

    /**
     * 全参构造函数
     */
    public UserUpdateDto(String id, String nickname, String username, String phone,
                         String email, String avatar, Integer status, List<String> roleIds,
                         String password,String confirmPassword,String targetUserId) {
        this.id = id;
        this.nickname = nickname;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
        this.roleIds = roleIds;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.targetUserId = targetUserId;
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
        this.status = status;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    // toString 方法

    @Override
    public String toString() {
        return "UserUpdateDto{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status=" + status +
                ", roleIds=" + roleIds +
                '}';
    }

    // equals 和 hashCode 方法

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserUpdateDto that = (UserUpdateDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(nickname, that.nickname) &&
                Objects.equals(username, that.username) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(email, that.email) &&
                Objects.equals(avatar, that.avatar) &&
                Objects.equals(status, that.status) &&
                Objects.equals(roleIds, that.roleIds)&&
                Objects.equals(password, that.password)&&
                Objects.equals(confirmPassword, that.confirmPassword)&&
                Objects.equals(targetUserId, that.targetUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, username, phone, email, avatar, status, roleIds,password,confirmPassword,targetUserId);
    }

    /**
     * 建造者模式
     */
    public static class Builder {
        private String id;
        private String nickname;
        private String username;
        private String phone;
        private String password;
        private String confirmPassword;
        private String targetUserId;

        private String email;
        private String avatar;
        private Integer status = 1;
        private List<String> roleIds;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public Builder confirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
            return this;
        }
         public Builder targetUserId(String targetUserId) {
            this.targetUserId = targetUserId;
            return this;
        }

        public Builder roleIds(List<String> roleIds) {
            this.roleIds = roleIds;
            return this;
        }

        public UserUpdateDto build() {
            UserUpdateDto dto = new UserUpdateDto();
            dto.setId(id);
            dto.setNickname(nickname);
            dto.setUsername(username);
            dto.setPhone(phone);
            dto.setEmail(email);
            dto.setAvatar(avatar);
            dto.setStatus(status);
            dto.setRoleIds(roleIds);
            dto.setPassword(password);
            dto.setConfirmPassword(confirmPassword);
            dto.setTargetUserId(targetUserId);
            return dto;
        }
    }

    /**
     * 创建建造者实例
     */
    public static Builder builder() {
        return new Builder();
    }
}