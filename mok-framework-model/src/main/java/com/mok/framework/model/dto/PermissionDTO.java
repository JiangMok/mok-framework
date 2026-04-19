package com.mok.baseframe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @description: 权限 dto 数据传输对象
 * @author: JN
 * @date: 2026/1/5 11:22
 **/
public class PermissionDTO  {

    private String id;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;

    private String description;

    // 1:菜单,2:按钮,3:接口
    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "父ID不能为空")
    private String parentId;

    private String icon;

    private String path;

    private String component;

    @NotNull(message = "排序不能为空")
    private Integer sort = 0;

    @NotNull(message = "是否可见不能为空")
    private Integer visible = 1;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;

    // 默认构造函数
    public PermissionDTO() {
    }

    // 全参数构造函数（可选）
    public PermissionDTO(String id, String permissionName, String permissionCode, String description,
                         Integer type, String parentId, String icon, String path, String component,
                         Integer sort, Integer visible, Integer status) {
        this.id = id;
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
        this.description = description;
        this.type = type;
        this.parentId = parentId;
        this.icon = icon;
        this.path = path;
        this.component = component;
        this.sort = sort != null ? sort : 0;
        this.visible = visible != null ? visible : 1;
        this.status = status != null ? status : 1;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort != null ? sort : 0;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible != null ? visible : 1;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status != null ? status : 1;
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
        PermissionDTO that = (PermissionDTO) o;
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
                Objects.equals(status, that.status);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id, permissionName, permissionCode, description, type, parentId,
                icon, path, component, sort, visible, status);
    }

    // toString 方法
    @Override
    public String toString() {
        return "PermissionDTO{" +
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
                '}';
    }
}