package com.mok.baseframe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * @description: 角色 DTO 数据传输对象
 * @author: JN
 * @date: 2026/1/5 11:20
 **/
public class RoleDTO  {

    private String id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String description;

    @NotNull(message = "排序不能为空")
    private Integer sort = 0;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;
    private String createBy;

    // 权限 ID 列表
    private List<String> permissionIds;

    // 默认构造函数
    public RoleDTO() {
    }

    // 全参数构造函数（可选）
    public RoleDTO(String id, String roleName, String roleCode, String description,
                   Integer sort, Integer status, List<String> permissionIds,String createBy) {
        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.description = description;
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : 1;
        this.permissionIds = permissionIds;
        this.createBy = createBy;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort != null ? sort : 0;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status != null ? status : 1;
    }

    public List<String> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<String> permissionIds) {
        this.permissionIds = permissionIds;
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
        RoleDTO roleDTO = (RoleDTO) o;
        return Objects.equals(id, roleDTO.id) &&
                Objects.equals(roleName, roleDTO.roleName) &&
                Objects.equals(roleCode, roleDTO.roleCode) &&
                Objects.equals(description, roleDTO.description) &&
                Objects.equals(sort, roleDTO.sort) &&
                Objects.equals(status, roleDTO.status) &&
                Objects.equals(permissionIds, roleDTO.permissionIds)&&
                Objects.equals(createBy, roleDTO.createBy);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(createBy,id, roleName, roleCode, description, sort, status, permissionIds);
    }

    // toString 方法
    @Override
    public String toString() {
        return "RoleDTO{" +
                "id='" + id + '\'' +
                ", roleName='" + roleName + '\'' +
                ", roleCode='" + roleCode + '\'' +
                ", description='" + description + '\'' +
                ", sort=" + sort +
                ", status=" + status +
                ", createBy=" + createBy +
                ", permissionIds=" + permissionIds +
                '}';
    }
}