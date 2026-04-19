package com.mok.baseframe.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.Objects;

@TableName("sys_file")
public class FileEntity {
    
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    
    @TableField("original_name")
    private String originalName;
    
    @TableField("storage_name")
    private String storageName;
    
    @TableField("file_path")
    private String filePath;
    
    @TableField("file_url")
    private String fileUrl;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("file_type")
    private String fileType;
    
    @TableField("mime_type")
    private String mimeType;
    
    @TableField("upload_user_id")
    private String uploadUserId;
    
    @TableField("upload_ip")
    private String uploadIp;
    
    @TableField("download_count")
    private Integer downloadCount = 0;

    @TableField("business_type")
    private Integer businessType = 0;
    
    @TableField("status")
    private Integer status = 1; // 1-正常，0-删除
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;
    
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    @TableField("is_deleted")
    private Integer isDeleted = 0;
    
    public FileEntity() {
    }
    
    // Getter 和 Setter 方法


    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }
    
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    
    public String getStorageName() {
        return storageName;
    }
    
    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getUploadUserId() {
        return uploadUserId;
    }
    
    public void setUploadUserId(String uploadUserId) {
        this.uploadUserId = uploadUserId;
    }
    
    public String getUploadIp() {
        return uploadIp;
    }
    
    public void setUploadIp(String uploadIp) {
        this.uploadIp = uploadIp;
    }
    
    public Integer getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getCreateBy() {
        return createBy;
    }
    
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
    
    public String getUpdateBy() {
        return updateBy;
    }
    
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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
        FileEntity that = (FileEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(originalName, that.originalName) &&
                Objects.equals(storageName, that.storageName) &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(fileUrl, that.fileUrl) &&
                Objects.equals(fileSize, that.fileSize) &&
                Objects.equals(fileType, that.fileType) &&
                Objects.equals(mimeType, that.mimeType) &&
                Objects.equals(uploadUserId, that.uploadUserId) &&
                Objects.equals(uploadIp, that.uploadIp) &&
                Objects.equals(downloadCount, that.downloadCount) &&
                Objects.equals(status, that.status) &&
                Objects.equals(businessType, that.businessType) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(createBy, that.createBy) &&
                Objects.equals(updateBy, that.updateBy) &&
                Objects.equals(isDeleted, that.isDeleted);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, originalName, storageName, filePath, fileUrl, fileSize, fileType, mimeType,
                uploadUserId, uploadIp, downloadCount, status, businessType,createTime, updateTime, createBy, updateBy, isDeleted);
    }
    
    // toString 方法
    @Override
    public String toString() {
        return "FileEntity{" +
                "id='" + id + '\'' +
                ", originalName='" + originalName + '\'' +
                ", storageName='" + storageName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", uploadUserId='" + uploadUserId + '\'' +
                ", uploadIp='" + uploadIp + '\'' +
                ", downloadCount=" + downloadCount +
                ", status=" + status +
                ", businessType=" + businessType +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createBy='" + createBy + '\'' +
                ", updateBy='" + updateBy + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}