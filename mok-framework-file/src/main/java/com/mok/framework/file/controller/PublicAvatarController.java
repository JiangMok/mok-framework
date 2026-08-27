package com.mok.framework.file.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.mok.framework.common.config.storage.FileStorageConfig;
import com.mok.framework.file.service.FileService;
import com.mok.framework.model.entity.FileEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import top.jiangmok.ratelimiter.annotation.RateLimit;
import top.jiangmok.ratelimiter.enums.RateLimitScope;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 公开头像读取入口。只有数据库中 business_type=1 的有效图片可以被读取，
 * 普通上传文件不再通过 /uploads/** 静态目录直接暴露。
 */
@RestController
public class PublicAvatarController {
    private static final Pattern YEAR = Pattern.compile("\\d{4}");
    private static final Pattern MONTH_OR_DAY = Pattern.compile("\\d{2}");
    private static final Pattern FILE_NAME = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(?:jpg|jpeg|png|gif|webp)",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> PUBLIC_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final FileService fileService;
    private final FileStorageConfig fileStorageConfig;

    public PublicAvatarController(FileService fileService, FileStorageConfig fileStorageConfig) {
        this.fileService = fileService;
        this.fileStorageConfig = fileStorageConfig;
    }

    @SaIgnore
    @RateLimit(scope = RateLimitScope.IP, limit = 120)
    @GetMapping("/uploads/{year}/{month}/{day}/{fileName}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String year,
                                               @PathVariable String month,
                                               @PathVariable String day,
                                               @PathVariable String fileName) {
        if (!YEAR.matcher(year).matches()
                || !MONTH_OR_DAY.matcher(month).matches()
                || !MONTH_OR_DAY.matcher(day).matches()
                || !FILE_NAME.matcher(fileName).matches()) {
            return ResponseEntity.notFound().build();
        }

        String relativePath = String.join("/", year, month, day, fileName);
        FileEntity fileEntity = fileService.getPublicAvatar(relativePath);
        String mimeType = fileEntity == null || fileEntity.getMimeType() == null
                ? ""
                : fileEntity.getMimeType().split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (fileEntity == null
                || !fileName.equals(fileEntity.getStorageName())
                || !PUBLIC_IMAGE_TYPES.contains(mimeType)
                || !matchesImageExtension(fileName, mimeType)) {
            return ResponseEntity.notFound().build();
        }

        Path basePath = Path.of(fileStorageConfig.getBasePath()).toAbsolutePath().normalize();
        Path avatarPath = basePath.resolve(relativePath).normalize();
        if (!avatarPath.startsWith(basePath) || !Files.isRegularFile(avatarPath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(avatarPath);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .header("Cross-Origin-Resource-Policy", "same-site")
                .contentType(MediaType.parseMediaType(mimeType))
                .contentLength(avatarPath.toFile().length())
                .body(resource);
    }

    private boolean matchesImageExtension(String fileName, String mimeType) {
        String lowerFileName = fileName.toLowerCase(Locale.ROOT);
        return switch (mimeType) {
            case "image/jpeg" -> lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg");
            case "image/png" -> lowerFileName.endsWith(".png");
            case "image/gif" -> lowerFileName.endsWith(".gif");
            case "image/webp" -> lowerFileName.endsWith(".webp");
            default -> false;
        };
    }
}
