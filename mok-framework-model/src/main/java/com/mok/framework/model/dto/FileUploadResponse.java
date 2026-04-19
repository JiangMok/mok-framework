package com.mok.framework.model.dto;

import java.util.Objects;

public class FileUploadResponse {
    private String id;
    private String originalName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    
    public FileUploadResponse() {
    }
    
    public FileUploadResponse(String id, String originalName, String fileUrl, Long fileSize, String fileType) {
        this.id = id;
        this.originalName = originalName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }
    
    // Getter 和 Setter 方法


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
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String originalName;
        private String fileUrl;
        private Long fileSize;
        private String fileType;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }
        
        public Builder fileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
            return this;
        }
        
        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }
        
        public Builder fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }
        
        public FileUploadResponse build() {
            return new FileUploadResponse(id, originalName, fileUrl, fileSize, fileType);
        }
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
        FileUploadResponse that = (FileUploadResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(originalName, that.originalName) &&
                Objects.equals(fileUrl, that.fileUrl) &&
                Objects.equals(fileSize, that.fileSize) &&
                Objects.equals(fileType, that.fileType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, originalName, fileUrl, fileSize, fileType);
    }
    
    // toString 方法
    @Override
    public String toString() {
        return "FileUploadResponse{" +
                "id='" + id + '\'' +
                ", originalName='" + originalName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}