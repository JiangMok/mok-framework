package com.mok.framework.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.framework.common.FileNotFoundException;
import com.mok.framework.common.FileUploadException;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.common.config.FileStorageConfig;
import com.mok.framework.file.mapper.FileMapper;
import com.mok.framework.file.service.FileService;
import com.mok.framework.model.dto.FileUploadResponse;
import com.mok.framework.model.entity.FileEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


/**
 * 文件service实现类
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileEntity> implements FileService {
    private static final Logger log = LogUtils.getLogger(FileServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final FileStorageConfig fileStorageConfig;
    private final HttpServletRequest request;

    public FileServiceImpl(FileStorageConfig fileStorageConfig,
                           HttpServletRequest request) {
        this.fileStorageConfig = fileStorageConfig;
        this.request = request;
    }

    @Override
    public PageResult<FileEntity> getPageList(PageParam param) {
        //创建分页对象
        Page<FileEntity> page = new Page<>(param.getPageNum(), param.getPageSize());
        //创建lambda查询包装器
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        //按文件类型查询
        if (param.getParams().get("fileType") != null) {
            wrapper.eq(FileEntity::getFileType, param.getParams().get("fileType"));
        }
        //按上传用户ID搜索
        if (param.getParams().get("uploadUserId") != null) {
            wrapper.eq(FileEntity::getUploadUserId, param.getParams().get("uploadUserId"));
        }
        //根据原始文件名或者存储文件名模糊搜索
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.like(FileEntity::getOriginalName, param.getKeyword())
                    .or().like(FileEntity::getStorageName, param.getKeyword());
        }
        if (param.getOrderBy() != null) {
            if ("asc".equalsIgnoreCase(param.getOrder())) {
                wrapper.orderByAsc(FileEntity::getCreateTime);
            } else {
                wrapper.orderByDesc(FileEntity::getCreateTime);
            }
        } else {
            //默认排序:先按sort升序,再按createTime降序
            wrapper.orderByDesc(FileEntity::getCreateTime);
        }
        //执行分页查询
        IPage<FileEntity> result = baseMapper.selectPage(page, wrapper);
        //转换为自定义的分页结果
        return PageResult.fromIPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse upload(MultipartFile file, Integer businessType) {
        try {
            // 1. 验证文件
            validateFile(file);

            // 2. 生成文件信息
            String originalName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalName);
            String mimeType = file.getContentType();
            long fileSize = file.getSize();

            // 3. 生成存储路径
            String datePath = LocalDateTime.now().format(DATE_FORMATTER);
            String storageName = UUID.randomUUID() + "." + extension;
            String relativePath = datePath + "/" + storageName;

            // 4. 保存到本地
            Path fullPath = Paths.get(fileStorageConfig.getBasePath(), relativePath);
            Files.createDirectories(fullPath.getParent());
            file.transferTo(fullPath.toFile());

            // 5. 保存到数据库
            FileEntity fileEntity = new FileEntity();
            fileEntity.setBusinessType(businessType);
            fileEntity.setId(IdUtil.simpleUUID());
            fileEntity.setOriginalName(originalName);
            fileEntity.setStorageName(storageName);
            fileEntity.setFilePath(relativePath);
            // 关键修改：使用完整的URL
            String fileUrl = fileStorageConfig.getFullFileUrl(relativePath);
            fileEntity.setFileUrl(fileUrl);
            fileEntity.setFileSize(fileSize);
            fileEntity.setFileType(getFileType(mimeType));
            fileEntity.setMimeType(mimeType);
            fileEntity.setUploadUserId(StpUtil.getLoginId().toString());
            fileEntity.setUploadIp(getClientIp());
            fileEntity.setCreateTime(LocalDateTime.now());

            save(fileEntity);
            // ... 在保存文件后添加日志
            log.info("文件保存信息: 路径={}, 访问URL={}, 完整URL={}",
                    fullPath.toString(),
                    fileEntity.getFileUrl(),
                    String.format("http://localhost:8080%s", fileEntity.getFileUrl()));
            // 6. 返回结果
            return FileUploadResponse.builder()
                    .id(fileEntity.getId())
                    .originalName(originalName)
                    .fileUrl(fileEntity.getFileUrl())
                    .fileSize(fileSize)
                    .fileType(fileEntity.getFileType())
                    .build();

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public FileEntity getFileInfo(String id) {
        FileEntity fileEntity = getFileEntity(id);
        return convertToFileInfo(fileEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void download(String id, HttpServletResponse response) {
        try {
            FileEntity fileEntity = getFileEntity(id);

            Path filePath = Paths.get(fileStorageConfig.getBasePath(), fileEntity.getFilePath());
            File file = filePath.toFile();

            if (!file.exists()) {
                response.setStatus(404);
                return;
            }

            // 设置响应头
            String encodedFileName = URLEncoder.encode(fileEntity.getOriginalName(), "UTF-8");
            response.setContentType(fileEntity.getMimeType());
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentLengthLong(fileEntity.getFileSize());

            // 写入响应流
            try (InputStream inputStream = new FileInputStream(file);
                 java.io.OutputStream outputStream = response.getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
            }

            // 更新下载次数
            updateDownloadCount(id);

        } catch (IOException e) {
            log.error("文件下载失败", e);
            throw new FileUploadException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        FileEntity fileEntity = getFileEntity(id);

        // 逻辑删除
        fileEntity.setStatus(0);
        fileEntity.setUpdateBy(StpUtil.getLoginId().toString());
        updateById(fileEntity);
//
//        //可选：物理删除文件
//        Path filePath = Paths.get(fileStorageConfig.getBasePath(), fileEntity.getFilePath());
//        try {
//            Files.deleteIfExists(filePath);
//        } catch (IOException e) {
//            throw new BusinessException("删除失败", e);
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        String currentUserId = StpUtil.getLoginId().toString();
        baseMapper.batchDelete(ids, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDownloadCount(String id) {
        baseMapper.incrementDownloadCount(id);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("文件不能为空");
        }

        if (!fileStorageConfig.isAllowedType(file.getContentType())) {
            throw new FileUploadException("不支持的文件类型");
        }

        // 文件大小限制在配置中通过Spring的multipart配置控制
    }

    private String getFileType(String mimeType) {
        if (mimeType == null) {
            return "other";
        }
        if (mimeType.startsWith("image/")) {
            return "image";
        } else if (mimeType.startsWith("video/")) {
            return "video";
        } else if (mimeType.startsWith("audio/")) {
            return "audio";
        } else if (mimeType.equals("application/pdf")) {
            return "document";
        } else {
            return "other";
        }
    }

    private FileEntity getFileEntity(String id) {
        LambdaQueryWrapper<FileEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileEntity::getId, id)
                .eq(FileEntity::getStatus, 1);

        FileEntity fileEntity = getOne(queryWrapper);
        if (fileEntity == null) {
            throw new FileNotFoundException(id);
        }
        return fileEntity;
    }

    private FileEntity convertToFileInfo(FileEntity fileEntity) {
        FileEntity fileInfo = new FileEntity();
        BeanUtils.copyProperties(fileEntity, fileInfo);

        // TODO: 如果需要，这里可以查询上传用户的姓名
        // fileInfo.setUploadUserName(userService.getUsernameById(fileEntity.getUploadUserId()));

        return fileInfo;
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过代理的情况，第一个IP为客户端真实IP（取逗号分割的第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}