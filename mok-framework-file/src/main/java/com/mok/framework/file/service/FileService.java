package com.mok.framework.file.service;


import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.model.dto.FileUploadResponse;
import com.mok.framework.model.entity.FileEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface FileService {

    /**
     * @description: 分页查询文件信息
     * @author: JN
     * @date: 2026/1/21 14:11
     * @param: [param]
     * @return: com.mok.baseframe.common.PageResult<com.mok.baseframe.dto.FileUploadResponse>
    **/
    PageResult<FileEntity> getPageList(PageParam param);
    
    /**
     * 上传文件
     */
    FileUploadResponse upload(MultipartFile file, Integer businessType);

    /**
     * 获取文件详情
     */
    FileEntity getFileInfo(String id);

    /**
     * 下载文件
     */
    void download(String fileId, HttpServletResponse response);

    /**
     * 删除文件（逻辑删除）
     */
    void delete(String id);

    /**
     * 批量删除文件
     */
    void batchDelete(List<String> ids);

    /**
     * 更新下载次数
     */
    void updateDownloadCount(String id);
}