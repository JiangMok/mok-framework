package com.mok.baseframe.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @description: 用户-角色-关联实体
 * @author: JN
 * @date: 2026/1/1 18:03
 **/
@TableName("sys_user_role")
public class UserRoleEntity implements Serializable {
    // 添加 serialVersionUID
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("role_id")
    private String roleId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 构造方法
    public UserRoleEntity() {
    }

    public UserRoleEntity(String id, String userId, String roleId, LocalDateTime createTime) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.createTime = createTime;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public UserRoleEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserRoleEntity setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getRoleId() {
        return roleId;
    }

    public UserRoleEntity setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public UserRoleEntity setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
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
        UserRoleEntity that = (UserRoleEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(createTime, that.createTime);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, roleId, createTime);
    }

    // toString 方法
    @Override
    public String toString() {
        return "UserRoleEntity{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", roleId='" + roleId + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public static class Builder {
        private String id;
        private String userId;
        private String roleId;
        private LocalDateTime createTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder roleId(String roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public UserRoleEntity build() {
            return new UserRoleEntity(id, userId, roleId, createTime);
        }
    }
}