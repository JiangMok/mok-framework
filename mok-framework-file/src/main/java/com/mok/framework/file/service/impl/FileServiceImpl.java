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
import com.mok.framework.common.config.storage.FileStorageConfig;
import com.mok.framework.file.mapper.FileMapper;
import com.mok.framework.file.service.FileService;
import com.mok.framework.file.validation.FileContentValidator;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * 文件service实现类
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileEntity> implements FileService {
    private static final Logger log = LogUtils.getLogger(FileServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Set<String> AVATAR_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Map<String, Set<String>> MIME_EXTENSIONS = Map.ofEntries(
            Map.entry("image/jpeg", Set.of("jpg", "jpeg")),
            Map.entry("image/png", Set.of("png")),
            Map.entry("image/gif", Set.of("gif")),
            Map.entry("image/webp", Set.of("webp")),
            Map.entry("application/pdf", Set.of("pdf")),
            Map.entry("application/msword", Set.of("doc")),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of("docx")),
            Map.entry("application/vnd.ms-excel", Set.of("xls")),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Set.of("xlsx")),
            Map.entry("application/vnd.ms-powerpoint", Set.of("ppt")),
            Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation", Set.of("pptx")),
            Map.entry("text/plain", Set.of("txt")),
            Map.entry("application/zip", Set.of("zip")),
            Map.entry("application/x-rar-compressed", Set.of("rar")));
    private final FileStorageConfig fileStorageConfig;
    private final HttpServletRequest request;
    private final FileContentValidator fileContentValidator;

    public FileServiceImpl(FileStorageConfig fileStorageConfig,
                           HttpServletRequest request,
                           FileContentValidator fileContentValidator) {
        this.fileStorageConfig = fileStorageConfig;
        this.request = request;
        this.fileContentValidator = fileContentValidator;
    }

    @Override
    public PageResult<FileEntity> getPageList(PageParam param) {
        //创建分页对象
        Page<FileEntity> page = param.toPageWithoutOrder();
        //创建lambda查询包装器
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntity::getStatus, 1)
                .eq(FileEntity::getIsDeleted, 0);
        //按文件类型查询
        if (param.getParams() != null && param.getParams().get("fileType") != null) {
            wrapper.eq(FileEntity::getFileType, param.getParams().get("fileType"));
        }
        //按上传用户ID搜索
        if (param.getParams() != null && param.getParams().get("uploadUserId") != null) {
            wrapper.eq(FileEntity::getUploadUserId, param.getParams().get("uploadUserId"));
        }
        //根据原始文件名或者存储文件名模糊搜索
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.and(search -> search.like(FileEntity::getOriginalName, param.getKeyword())
                    .or().like(FileEntity::getStorageName, param.getKeyword()));
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
        result.getRecords().forEach(this::hideNonPublicFileUrl);
        //转换为自定义的分页结果
        return PageResult.fromIPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse upload(MultipartFile file, Integer businessType) {
        Path fullPath = null;
        try {
            // 1. 验证文件
            validateFile(file, businessType);

            // 2. 生成文件信息
            String originalName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalName).toLowerCase(Locale.ROOT);
            String mimeType = normalizeMimeType(file.getContentType());
            long fileSize = file.getSize();

            // 3. 生成存储路径
            String datePath = LocalDateTime.now().format(DATE_FORMATTER);
            String storageName = UUID.randomUUID() + "." + extension;
            String relativePath = datePath + "/" + storageName;

            // 4. 保存到本地
            Path basePath = Paths.get(fileStorageConfig.getBasePath()).toAbsolutePath().normalize();
            fullPath = basePath.resolve(relativePath).normalize();
            if (!fullPath.startsWith(basePath)) {
                throw new FileUploadException("文件存储路径无效");
            }
            Files.createDirectories(fullPath.getParent());
            file.transferTo(fullPath.toFile());
            registerRollbackCleanup(fullPath);

            // 5. 保存到数据库
            FileEntity fileEntity = new FileEntity();
            fileEntity.setBusinessType(businessType);
            fileEntity.setId(IdUtil.simpleUUID());
            fileEntity.setOriginalName(originalName);
            fileEntity.setStorageName(storageName);
            fileEntity.setFilePath(relativePath);
            // 只有头像允许使用公开 /uploads/** URL；普通文件必须通过鉴权下载接口获取。
            String fileUrl = Integer.valueOf(1).equals(businessType)
                    ? fileStorageConfig.getFullFileUrl(relativePath)
                    : fileStorageConfig.getFullDownloadUrl(fileEntity.getId());
            fileEntity.setFileUrl(fileUrl);
            fileEntity.setFileSize(fileSize);
            fileEntity.setFileType(getFileType(mimeType));
            fileEntity.setMimeType(mimeType);
            fileEntity.setUploadUserId(StpUtil.getLoginId().toString());
            fileEntity.setUploadIp(getClientIp());
            fileEntity.setCreateTime(LocalDateTime.now());

            if (!save(fileEntity)) {
                throw new FileUploadException("文件元数据保存失败");
            }
            log.info("文件保存信息: 路径={}, 访问URL={}", fullPath, fileEntity.getFileUrl());
            // 6. 返回结果
            return FileUploadResponse.builder()
                    .id(fileEntity.getId())
                    .originalName(originalName)
                    .fileUrl(fileEntity.getFileUrl())
                    .fileSize(fileSize)
                    .fileType(fileEntity.getFileType())
                    .build();

        } catch (FileUploadException e) {
            deleteStoredFileQuietly(fullPath);
            throw e;
        } catch (IOException e) {
            deleteStoredFileQuietly(fullPath);
            log.error("文件上传失败", e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            deleteStoredFileQuietly(fullPath);
            throw e;
        }
    }

    @Override
    public FileEntity getFileInfo(String id) {
        FileEntity fileEntity = getFileEntity(id);
        return convertToFileInfo(fileEntity);
    }

    @Override
    public FileEntity getPublicAvatar(String relativePath) {
        LambdaQueryWrapper<FileEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileEntity::getFilePath, relativePath)
                .eq(FileEntity::getBusinessType, 1)
                .eq(FileEntity::getStatus, 1)
                .eq(FileEntity::getIsDeleted, 0);
        FileEntity fileEntity = getOne(queryWrapper, false);
        if (fileEntity == null) {
            return null;
        }

        String mimeType = normalizeMimeType(fileEntity.getMimeType());
        if (!StringUtils.hasText(fileEntity.getStorageName())) {
            return null;
        }
        String extension = FilenameUtils.getExtension(fileEntity.getStorageName()).toLowerCase(Locale.ROOT);
        if (!AVATAR_MIME_TYPES.contains(mimeType)
                || !MIME_EXTENSIONS.getOrDefault(mimeType, Set.of()).contains(extension)) {
            return null;
        }
        Path storedPath = resolveStoredPath(fileEntity);
        try {
            if (!fileContentValidator.matches(storedPath, mimeType)) {
                log.warn("公开头像真实内容与声明类型不匹配: fileId={}", fileEntity.getId());
                return null;
            }
        } catch (IOException exception) {
            log.warn("读取公开头像内容失败: fileId={}", fileEntity.getId(), exception);
            return null;
        }
        return fileEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void download(String id, HttpServletResponse response) {
        try {
            FileEntity fileEntity = getFileEntity(id);

            Path filePath = resolveStoredPath(fileEntity);
            File file = filePath.toFile();

            if (!file.isFile()) {
                response.setStatus(404);
                return;
            }

            // 设置响应头
            String encodedFileName = URLEncoder.encode(fileEntity.getOriginalName(), "UTF-8");
            response.setContentType(fileEntity.getMimeType());
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setContentLengthLong(fileEntity.getFileSize());

            // 写入响应流
            try (InputStream inputStream = new FileInputStream(file);
                 java.io.OutputStream outputStream = response.getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
            }

            // 更新下载次数
            baseMapper.incrementDownloadCount(id);

        } catch (IOException e) {
            log.error("文件下载失败", e);
            throw new FileUploadException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String currentUserId = StpUtil.getLoginId().toString();
        if (baseMapper.logicalDelete(id, currentUserId) != 1) {
            throw new FileNotFoundException(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("文件ID列表不能为空");
        }

        List<String> uniqueIds = new ArrayList<>(new LinkedHashSet<>(ids));
        List<FileEntity> activeFiles = baseMapper.selectList(new LambdaQueryWrapper<FileEntity>()
                .in(FileEntity::getId, uniqueIds)
                .eq(FileEntity::getStatus, 1)
                .eq(FileEntity::getIsDeleted, 0));
        if (activeFiles.size() != uniqueIds.size()) {
            throw new FileNotFoundException(String.join(",", uniqueIds));
        }
        String currentUserId = StpUtil.getLoginId().toString();
        if (baseMapper.batchDelete(uniqueIds, currentUserId) != uniqueIds.size()) {
            throw new FileUploadException("批量删除文件失败，请重试");
        }
    }

    private void validateFile(MultipartFile file, Integer businessType) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("文件不能为空");
        }

        if (!Integer.valueOf(1).equals(businessType) && !Integer.valueOf(2).equals(businessType)) {
            throw new FileUploadException("文件业务类型不正确");
        }

        String originalName = file.getOriginalFilename();
        String mimeType = normalizeMimeType(file.getContentType());
        String extension = StringUtils.hasText(originalName)
                ? FilenameUtils.getExtension(originalName).toLowerCase(Locale.ROOT)
                : "";

        if (!StringUtils.hasText(originalName)
                || !fileStorageConfig.isAllowedType(mimeType)
                || !MIME_EXTENSIONS.getOrDefault(mimeType, Set.of()).contains(extension)) {
            throw new FileUploadException("不支持的文件类型");
        }

        if (Integer.valueOf(1).equals(businessType) && !AVATAR_MIME_TYPES.contains(mimeType)) {
            throw new FileUploadException("头像仅支持 JPG、PNG、GIF 或 WebP 图片");
        }

        try {
            if (!fileContentValidator.matches(file, mimeType)) {
                throw new FileUploadException("文件真实内容与声明类型不匹配");
            }
        } catch (IOException exception) {
            throw new FileUploadException("无法读取文件内容", exception);
        }

        // 文件大小限制在配置中通过Spring的multipart配置控制
    }

    private String normalizeMimeType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
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
                .eq(FileEntity::getStatus, 1)
                .eq(FileEntity::getIsDeleted, 0);

        FileEntity fileEntity = getOne(queryWrapper);
        if (fileEntity == null) {
            throw new FileNotFoundException(id);
        }
        return fileEntity;
    }

    private FileEntity convertToFileInfo(FileEntity fileEntity) {
        FileEntity fileInfo = new FileEntity();
        BeanUtils.copyProperties(fileEntity, fileInfo);
        hideNonPublicFileUrl(fileInfo);

        // TODO: 如果需要，这里可以查询上传用户的姓名
        // fileInfo.setUploadUserName(userService.getUsernameById(fileEntity.getUploadUserId()));

        return fileInfo;
    }

    private void hideNonPublicFileUrl(FileEntity fileEntity) {
        if (!Integer.valueOf(1).equals(fileEntity.getBusinessType())) {
            fileEntity.setFileUrl(fileStorageConfig.getFullDownloadUrl(fileEntity.getId()));
        }
    }

    private void registerRollbackCleanup(Path storedPath) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    deleteStoredFileQuietly(storedPath);
                }
            }
        });
    }

    private void deleteStoredFileQuietly(Path storedPath) {
        if (storedPath == null) {
            return;
        }
        try {
            Files.deleteIfExists(storedPath);
        } catch (IOException cleanupException) {
            log.error("回滚上传文件失败: {}", storedPath, cleanupException);
        }
    }

    private Path resolveStoredPath(FileEntity fileEntity) {
        if (!StringUtils.hasText(fileEntity.getFilePath())) {
            throw new FileNotFoundException(fileEntity.getId());
        }
        Path basePath = Paths.get(fileStorageConfig.getBasePath()).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(fileEntity.getFilePath()).normalize();
        if (!filePath.startsWith(basePath)) {
            log.warn("拒绝访问越界文件路径: fileId={}", fileEntity.getId());
            throw new FileNotFoundException(fileEntity.getId());
        }
        return filePath;
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
