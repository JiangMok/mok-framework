package com.mok.framework.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @description: 部门实体类
 * @author: mok
 * @date: 2026/7/15
 **/
@TableName("sys_dept")
public class DepartmentEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("dept_name")
    private String deptName;

    @TableField("dept_code")
    private String deptCode;

    @TableField("parent_id")
    private String parentId;

    @TableField("ancestors")
    private String ancestors;

    @TableField("description")
    private String description;

    @TableField("leader")
    private String leader;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("create_by")
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public DepartmentEntity() {
    }

    public DepartmentEntity(String id, String deptName, String deptCode, String parentId,
                            String ancestors, String description, String leader, String phone,
                            String email, Integer sort, Integer status, Integer isDeleted,
                            String createBy, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.deptName = deptName;
        this.deptCode = deptCode;
        this.parentId = parentId;
        this.ancestors = ancestors;
        this.description = description;
        this.leader = leader;
        this.phone = phone;
        this.email = email;
        this.sort = sort;
        this.status = status;
        this.isDeleted = isDeleted;
        this.createBy = createBy;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String deptName;
        private String deptCode;
        private String parentId;
        private String ancestors;
        private String description;
        private String leader;
        private String phone;
        private String email;
        private Integer sort;
        private Integer status;
        private Integer isDeleted;
        private String createBy;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public Builder id(String id) { this.id = id; return this; }
        public Builder deptName(String deptName) { this.deptName = deptName; return this; }
        public Builder deptCode(String deptCode) { this.deptCode = deptCode; return this; }
        public Builder parentId(String parentId) { this.parentId = parentId; return this; }
        public Builder ancestors(String ancestors) { this.ancestors = ancestors; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder leader(String leader) { this.leader = leader; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder sort(Integer sort) { this.sort = sort; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder isDeleted(Integer isDeleted) { this.isDeleted = isDeleted; return this; }
        public Builder createBy(String createBy) { this.createBy = createBy; return this; }
        public Builder createTime(LocalDateTime createTime) { this.createTime = createTime; return this; }
        public Builder updateTime(LocalDateTime updateTime) { this.updateTime = updateTime; return this; }

        public DepartmentEntity build() {
            return new DepartmentEntity(id, deptName, deptCode, parentId, ancestors,
                    description, leader, phone, email, sort, status, isDeleted,
                    createBy, createTime, updateTime);
        }
    }

    // Getter/Setter（链式）
    public String getId() { return id; }
    public DepartmentEntity setId(String id) { this.id = id; return this; }

    public String getDeptName() { return deptName; }
    public DepartmentEntity setDeptName(String deptName) { this.deptName = deptName; return this; }

    public String getDeptCode() { return deptCode; }
    public DepartmentEntity setDeptCode(String deptCode) { this.deptCode = deptCode; return this; }

    public String getParentId() { return parentId; }
    public DepartmentEntity setParentId(String parentId) { this.parentId = parentId; return this; }

    public String getAncestors() { return ancestors; }
    public DepartmentEntity setAncestors(String ancestors) { this.ancestors = ancestors; return this; }

    public String getDescription() { return description; }
    public DepartmentEntity setDescription(String description) { this.description = description; return this; }

    public String getLeader() { return leader; }
    public DepartmentEntity setLeader(String leader) { this.leader = leader; return this; }

    public String getPhone() { return phone; }
    public DepartmentEntity setPhone(String phone) { this.phone = phone; return this; }

    public String getEmail() { return email; }
    public DepartmentEntity setEmail(String email) { this.email = email; return this; }

    public Integer getSort() { return sort; }
    public DepartmentEntity setSort(Integer sort) { this.sort = sort; return this; }

    public Integer getStatus() { return status; }
    public DepartmentEntity setStatus(Integer status) { this.status = status; return this; }

    public Integer getIsDeleted() { return isDeleted; }
    public DepartmentEntity setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; return this; }

    public String getCreateBy() { return createBy; }
    public DepartmentEntity setCreateBy(String createBy) { this.createBy = createBy; return this; }

    public LocalDateTime getCreateTime() { return createTime; }
    public DepartmentEntity setCreateTime(LocalDateTime createTime) { this.createTime = createTime; return this; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public DepartmentEntity setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; return this; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentEntity that = (DepartmentEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(deptName, that.deptName) &&
                Objects.equals(deptCode, that.deptCode) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(ancestors, that.ancestors) &&
                Objects.equals(description, that.description) &&
                Objects.equals(leader, that.leader) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(email, that.email) &&
                Objects.equals(sort, that.sort) &&
                Objects.equals(status, that.status) &&
                Objects.equals(isDeleted, that.isDeleted) &&
                Objects.equals(createBy, that.createBy) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deptName, deptCode, parentId, ancestors, description,
                leader, phone, email, sort, status, isDeleted, createBy, createTime, updateTime);
    }

    @Override
    public String toString() {
        return "DepartmentEntity{" +
                "id='" + id + '\'' +
                ", deptName='" + deptName + '\'' +
                ", deptCode='" + deptCode + '\'' +
                ", parentId='" + parentId + '\'' +
                ", ancestors='" + ancestors + '\'' +
                ", description='" + description + '\'' +
                ", leader='" + leader + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", sort=" + sort +
                ", status=" + status +
                ", isDeleted=" + isDeleted +
                ", createBy='" + createBy + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
