package com.mok.framework.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @description: 权限实体类
 * @author: JN
 * @date: 2026/1/1 18:01
 **/
@TableName("sys_permission")
public class PermissionEntity implements Serializable  {
    // 添加 serialVersionUID
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("permission_name")
    private String permissionName;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("description")
    private String description;

    @TableField("type")
    private Integer type;  // 1:菜单,2:按钮,3:接口

    @TableField("parent_id")
    private String parentId;

    @TableField("icon")
    private String icon;

    @TableField("path")
    private String path;

    @TableField("component")
    private String component;

    @TableField("sort")
    private Integer sort;

    @TableField("visible")
    private Integer visible;  // 0:隐藏,1:显示

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
    public PermissionEntity() {
    }

    public PermissionEntity(String id, String permissionName, String permissionCode,
                            String description, Integer type, String parentId,
                            String icon, String path, String component, Integer sort,
                            Integer visible, Integer status, Integer isDeleted,
                            LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
        this.description = description;
        this.type = type;
        this.parentId = parentId;
        this.icon = icon;
        this.path = path;
        this.component = component;
        this.sort = sort;
        this.visible = visible;
        this.status = status;
        this.isDeleted = isDeleted;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String permissionName;
        private String permissionCode;
        private String description;
        private Integer type;
        private String parentId;
        private String icon;
        private String path;
        private String component;
        private Integer sort;
        private Integer visible;
        private Integer status;
        private Integer isDeleted;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder permissionName(String permissionName) {
            this.permissionName = permissionName;
            return this;
        }

        public Builder permissionCode(String permissionCode) {
            this.permissionCode = permissionCode;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(Integer type) {
            this.type = type;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder component(String component) {
            this.component = component;
            return this;
        }

        public Builder sort(Integer sort) {
            this.sort = sort;
            return this;
        }

        public Builder visible(Integer visible) {
            this.visible = visible;
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

        public PermissionEntity build() {
            return new PermissionEntity(id, permissionName, permissionCode, description,
                    type, parentId, icon, path, component, sort, visible, status,
                    isDeleted, createTime, updateTime);
        }
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public PermissionEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public PermissionEntity setPermissionName(String permissionName) {
        this.permissionName = permissionName;
        return this;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public PermissionEntity setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PermissionEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public PermissionEntity setType(Integer type) {
        this.type = type;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public PermissionEntity setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public PermissionEntity setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getPath() {
        return path;
    }

    public PermissionEntity setPath(String path) {
        this.path = path;
        return this;
    }

    public String getComponent() {
        return component;
    }

    public PermissionEntity setComponent(String component) {
        this.component = component;
        return this;
    }

    public Integer getSort() {
        return sort;
    }

    public PermissionEntity setSort(Integer sort) {
        this.sort = sort;
        return this;
    }

    public Integer getVisible() {
        return visible;
    }

    public PermissionEntity setVisible(Integer visible) {
        this.visible = visible;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public PermissionEntity setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public PermissionEntity setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public PermissionEntity setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public PermissionEntity setUpdateTime(LocalDateTime updateTime) {
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
        PermissionEntity that = (PermissionEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(permissionName, that.permissionName) &&
                Objects.equals(permissionCode, that.permissionCode) &&
                Objects.equals(description, that.description) &&
                Objects.equals(type, that.type) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(path, that.path) &&
                Objects.equals(component, that.component) &&
                Objects.equals(sort, that.sort) &&
                Objects.equals(visible, that.visible) &&
                Objects.equals(status, that.status) &&
                Objects.equals(isDeleted, that.isDeleted) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id, permissionName, permissionCode, description, type, parentId, icon, path, component,
                sort, visible, status, isDeleted, createTime, updateTime);
    }

    // toString 方法
    @Override
    public String toString() {
        return "PermissionEntity{" +
                "id='" + id + '\'' +
                ", permissionName='" + permissionName + '\'' +
                ", permissionCode='" + permissionCode + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", parentId='" + parentId + '\'' +
                ", icon='" + icon + '\'' +
                ", path='" + path + '\'' +
                ", component='" + component + '\'' +
                ", sort=" + sort +
                ", visible=" + visible +
                ", status=" + status +
                ", isDeleted=" + isDeleted +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}