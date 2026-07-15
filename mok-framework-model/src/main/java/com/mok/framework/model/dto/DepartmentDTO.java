package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description: 部门 DTO
 * @author: mok
 * @date: 2026/7/15
 **/
public class DepartmentDTO {

    private String id;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    @NotBlank(message = "部门编码不能为空")
    private String deptCode;

    @NotNull(message = "父部门ID不能为空")
    private String parentId;

    private String description;

    private String leader;

    private String phone;

    private String email;

    @NotNull(message = "排序不能为空")
    private Integer sort = 0;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;

    /** 子部门列表（树形返回） */
    private List<DepartmentDTO> children;

    public DepartmentDTO() {
    }

    public DepartmentDTO(String id, String deptName, String deptCode, String parentId,
                         String description, String leader, String phone, String email,
                         Integer sort, Integer status) {
        this.id = id;
        this.deptName = deptName;
        this.deptCode = deptCode;
        this.parentId = parentId;
        this.description = description;
        this.leader = leader;
        this.phone = phone;
        this.email = email;
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : 1;
    }

    // Getter/Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLeader() { return leader; }
    public void setLeader(String leader) { this.leader = leader; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort != null ? sort : 0; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status != null ? status : 1; }

    public List<DepartmentDTO> getChildren() { return children; }
    public void setChildren(List<DepartmentDTO> children) { this.children = children; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentDTO that = (DepartmentDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(deptName, that.deptName) &&
                Objects.equals(deptCode, that.deptCode) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(leader, that.leader) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(email, that.email) &&
                Objects.equals(sort, that.sort) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deptName, deptCode, parentId, description,
                leader, phone, email, sort, status);
    }

    @Override
    public String toString() {
        return "DepartmentDTO{" +
                "id='" + id + '\'' +
                ", deptName='" + deptName + '\'' +
                ", deptCode='" + deptCode + '\'' +
                ", parentId='" + parentId + '\'' +
                ", description='" + description + '\'' +
                ", leader='" + leader + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", sort=" + sort +
                ", status=" + status +
                '}';
    }
}
