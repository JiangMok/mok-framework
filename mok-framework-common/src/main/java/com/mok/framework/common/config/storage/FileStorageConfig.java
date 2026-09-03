package com.mok.framework.common.config.storage;

import com.mok.framework.common.utils.LogUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {
    private static final Logger log = LogUtils.getLogger(FileStorageConfig.class);

    private String basePath = "/tmp/uploads";
    private String urlPrefix = "/uploads";
    private String downloadUrlPrefix = "";
    private List<String> allowedTypes;
    private String baseUrl = "";  // 新增

    public FileStorageConfig() {
    }

    public boolean isAllowedType(String contentType) {
        if (contentType == null) {
            return false;
        }
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return true;
        }
        String normalizedType = contentType.split(";", 2)[0].trim();
        return allowedTypes.stream().anyMatch(type -> type.equalsIgnoreCase(normalizedType));
    }

    // Getter 和 Setter 方法
    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getDownloadUrlPrefix() {
        return downloadUrlPrefix;
    }

    public void setDownloadUrlPrefix(String downloadUrlPrefix) {
        this.downloadUrlPrefix = downloadUrlPrefix;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
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
        FileStorageConfig that = (FileStorageConfig) o;
        return Objects.equals(basePath, that.basePath) &&
                Objects.equals(urlPrefix, that.urlPrefix) &&
                Objects.equals(downloadUrlPrefix, that.downloadUrlPrefix) &&
                Objects.equals(baseUrl, that.baseUrl) &&
                Objects.equals(allowedTypes, that.allowedTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePath, urlPrefix, downloadUrlPrefix, allowedTypes, baseUrl);
    }

    // toString 方法
    @Override
    public String toString() {
        return "FileStorageConfig{" +
                "basePath='" + basePath + '\'' +
                ", urlPrefix='" + urlPrefix + '\'' +
                ", downloadUrlPrefix='" + downloadUrlPrefix + '\'' +
                ", allowedTypes=" + allowedTypes +
                ", baseUrl='" + baseUrl + '\'' +
                '}';
    }

    @PostConstruct
    public void init() {
        if (baseUrl == null || baseUrl.isBlank()) {
            try {
                // 自动获取服务器地址
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                String port = System.getProperty("server.port", "8080");

                // 生产环境建议在配置文件中明确指定baseUrl
                // 这里只是一个后备方案
                this.baseUrl = "http://" + hostAddress + ":" + port;

                log.warn("baseUrl未配置，使用自动检测: {}", this.baseUrl);
            } catch (Exception e) {
                this.baseUrl = "http://localhost:8080";
                log.error("无法获取服务器地址，使用默认值: {}", this.baseUrl, e);
            }
        }
    }

    // 新增方法：获取完整的文件访问URL
    public String getFullFileUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }

        // 如果relativePath已经是完整的URL，直接返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }

        // 构建完整URL
        String configuredBaseUrl = baseUrl == null ? "" : baseUrl;
        String base = configuredBaseUrl.endsWith("/")
                ? configuredBaseUrl.substring(0, configuredBaseUrl.length() - 1)
                : configuredBaseUrl;
        String configuredUrlPrefix = urlPrefix == null ? "/uploads" : urlPrefix;
        String prefix = configuredUrlPrefix.startsWith("/")
                ? configuredUrlPrefix
                : "/" + configuredUrlPrefix;
        String path = relativePath.startsWith("/") ? relativePath : "/" + relativePath;

        return base + prefix + path;
    }

    /**
     * 构建普通文件的鉴权下载地址。
     * 未单独配置前缀时，根据公开上传前缀推导
     * （例如 /api/uploads 推导为 /api/files/download）。
     */
    public String getFullDownloadUrl(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            return "";
        }
        String prefix = downloadUrlPrefix;
        if (prefix == null || prefix.isBlank()) {
            String normalizedPublicPrefix = urlPrefix == null ? "" : urlPrefix.replaceAll("/+$", "");
            prefix = normalizedPublicPrefix.endsWith("/uploads")
                    ? normalizedPublicPrefix.substring(0, normalizedPublicPrefix.length() - "/uploads".length())
                            + "/files/download"
                    : "/files/download";
        }
        String configuredBaseUrl = baseUrl == null ? "" : baseUrl;
        String base = configuredBaseUrl.endsWith("/")
                ? configuredBaseUrl.substring(0, configuredBaseUrl.length() - 1)
                : configuredBaseUrl;
        String normalizedPrefix = prefix.startsWith("/") ? prefix : "/" + prefix;
        return base + normalizedPrefix.replaceAll("/+$", "") + "/" + fileId;
    }

    // 新增 getter 和 setter
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


}
