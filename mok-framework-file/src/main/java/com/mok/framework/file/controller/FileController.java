package com.mok.framework.file.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.file.service.FileService;
import com.mok.framework.model.dto.BatchDeleteRequest;
import com.mok.framework.model.dto.FileUploadResponse;
import com.mok.framework.model.entity.FileEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: 文件上传controller
 **/
@Tag(name = "文件管理", description = "文件上传下载接口")
@RestController
@RequestMapping("/files")
public class FileController {
    private static final Logger log = LogUtils.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    /**
     * @description: 分页查询文件信息列表
     * @author: JN
     * @date: 2026/1/5 16:50
     * @param: [param]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.common.PageResult < com.mok.securityframework.entity.Role>>
     **/
    @Operation(summary = "分页查询文件列表")
    @OperationLog(title = "分页查询文件", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @SaCheckPermission("system:files:query")
    public R<PageResult<FileEntity>> page(@RequestBody @Valid PageParam param) {
        return R.ok(fileService.getPageList(param));
    }

    /**
     * 上传文件 - 需要登录
     * 测试时，先登录获取token，然后在Header中添加：Authorization: Bearer {token}
     */
    @Operation(summary = "上传文件")
    @OperationLog(title = "上传文件", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    @SaCheckPermission("system:files:upload")
    public R<FileUploadResponse> upload(
            @Parameter(description = "文件")
            @RequestParam("file") MultipartFile file) {

        log.info("文件上传: originalName={}, size={}, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            FileUploadResponse result = fileService.upload(file,2);
            return R.ok("文件上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.error(e.getMessage());
        }
    }

    @Operation(summary = "上传用户头像")
    @OperationLog(title = "上传用户头像", businessType = BusinessType.INSERT)
    @PostMapping("/uploadAvatar")
    @SaCheckPermission("system:files:uploadAvatar")
    public R<FileUploadResponse> uploadAvatar(
            @Parameter(description = "文件")
            @RequestParam("file") MultipartFile file) {
        try {
            FileUploadResponse result = fileService.upload(file,1);
            return R.ok("文件上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.error(e.getMessage());
        }
    }

    @Operation(summary = "获取文件详情")
    @GetMapping("/{id}")
    public R<FileEntity> getFileInfo(
            @Parameter(description = "文件ID")
            @PathVariable String id) {

        try {
            FileEntity fileInfo = fileService.getFileInfo(id);
            return R.ok(fileInfo);
        } catch (Exception e) {
            log.error("获取文件详情失败: {}", id, e);
            return R.error("获取文件详情失败");
        }
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{id}")
    public void download(
            @Parameter(description = "文件ID")
            @PathVariable("id") String id,
            HttpServletResponse response) {

        log.info("文件下载: id={}", id);
        fileService.download(id, response);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:files:delete")
    public R<Void> delete(
            @Parameter(description = "文件ID")
            @PathVariable("id") String id) {

        log.info("删除文件: id={}", id);
        try {
            fileService.delete(id);
            return R.ok();
        } catch (Exception e) {
            log.error("删除文件失败: {}", id, e);
            return R.error("删除失败");
        }
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/batchDelete")
    @SaCheckPermission("system:files:delete")
    public R<String> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {

        log.info("批量删除文件: ids={}", request.getIds());
        try {
            fileService.batchDelete(request.getIds());
            return R.ok();
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            return R.error("批量删除失败");
        }
    }

    @Operation(summary = "更新下载次数")
    @PutMapping("/updateDownloadCount/{id}")
    public R<Void> updateDownloadCount(
            @Parameter(description = "文件ID")
            @PathVariable("id") String id) {

        log.info("更新下载次数: id={}", id);
        try {
            fileService.updateDownloadCount(id);
            return R.ok();
        } catch (Exception e) {
            log.error("更新下载次数失败: {}", id, e);
            return R.error("更新失败");
        }
    }


}