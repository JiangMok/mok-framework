package com.mok.baseframe.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @description: 角色实体类
 * @author: JN
 * @date: 2026/1/1 18:00
 **/
@TableName("sys_role")
public class RoleEntity implements Serializable  {
    // 添加 serialVersionUID
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("role_name")
    private String roleName;

    @TableField("role_code")
    private String roleCode;

    @TableField("description")
    private String description;

    @TableField("create_by")
    private String createBy;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 构造方法
    public RoleEntity() {
    }

    public RoleEntity(String id, String roleName, String roleCode, String description,
                      Integer sort, Integer status, Integer isDeleted,
                      LocalDateTime createTime, LocalDateTime updateTime,String createBy) {
        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.description = description;
        this.sort = sort;
        this.status = status;
        this.isDeleted = isDeleted;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createBy = createBy;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String roleName;
        private String roleCode;
        private String description;
        private String createBy;
        private Integer sort;
        private Integer status;
        private Integer isDeleted;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder createBy(String createBy) {
            this.createBy = createBy;
            return this;
        }

        public Builder roleName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Builder roleCode(String roleCode) {
            this.roleCode = roleCode;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder sort(Integer sort) {
            this.sort = sort;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder isDeleted(Integer isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public RoleEntity build() {
            return new RoleEntity(id, roleName, roleCode, description, sort,
                    status, isDeleted, createTime, updateTime,createBy);
        }
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public RoleEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public RoleEntity setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }
    public String getCreateBy() {
        return createBy;
    }

    public RoleEntity setCreateBy(String createBy) {
        this.createBy = createBy;
        return this;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public RoleEntity setRoleCode(String roleCode) {
        this.roleCode = roleCode;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RoleEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getSort() {
        return sort;
    }

    public RoleEntity setSort(Integer sort) {
        this.sort = sort;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public RoleEntity setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public RoleEntity setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public RoleEntity setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public RoleEntity setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
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
        RoleEntity that = (RoleEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(roleName, that.roleName) &&
                Objects.equals(roleCode, that.roleCode) &&
                Objects.equals(description, that.description) &&
                Objects.equals(sort, that.sort) &&
                Objects.equals(status, that.status) &&
                Objects.equals(isDeleted, that.isDeleted) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id, roleName, roleCode, description, sort, status, isDeleted, createTime, updateTime);
    }

    // toString 方法
    @Override
    public String toString() {
        return "RoleEntity{" +
                "id='" + id + '\'' +
                ", roleName='" + roleName + '\'' +
                ", roleCode='" + roleCode + '\'' +
                ", description='" + description + '\'' +
                ", sort=" + sort +
                ", status=" + status +
                ", isDeleted=" + isDeleted +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}