package com.mok.baseframe.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @description: 角色-权限-关联实体
 * @author: JN
 * @date: 2026/1/1 18:04
 **/
@TableName("sys_role_permission")
public class RolePermissionEntity  {
    @TableId(type = IdType.AUTO)
    private String id;

    @TableField("role_id")
    private String roleId;

    @TableField("permission_id")
    private String permissionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 构造方法
    public RolePermissionEntity() {
    }

    public RolePermissionEntity(String id, String roleId, String permissionId, LocalDateTime createTime) {
        this.id = id;
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.createTime = createTime;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String roleId;
        private String permissionId;
        private LocalDateTime createTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder roleId(String roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder permissionId(String permissionId) {
            this.permissionId = permissionId;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public RolePermissionEntity build() {
            return new RolePermissionEntity(id, roleId, permissionId, createTime);
        }
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public RolePermissionEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getRoleId() {
        return roleId;
    }

    public RolePermissionEntity setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public RolePermissionEntity setPermissionId(String permissionId) {
        this.permissionId = permissionId;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public RolePermissionEntity setCreateTime(LocalDateTime createTime) {
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
        RolePermissionEntity that = (RolePermissionEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(permissionId, that.permissionId) &&
                Objects.equals(createTime, that.createTime);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id, roleId, permissionId, createTime);
    }

    // toString 方法
    @Override
    public String toString() {
        return "RolePermissionEntity{" +
                "id='" + id + '\'' +
                ", roleId='" + roleId + '\'' +
                ", permissionId='" + permissionId + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}