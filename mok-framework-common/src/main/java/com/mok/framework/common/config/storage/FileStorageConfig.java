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
    private List<String> allowedTypes;
    private String baseUrl = "";  // 新增

    public FileStorageConfig() {
    }

    public boolean isAllowedType(String contentType) {
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return true;
        }
        return allowedTypes.stream().anyMatch(contentType::startsWith);
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
                Objects.equals(allowedTypes, that.allowedTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePath, urlPrefix, allowedTypes);
    }

    // toString 方法
    @Override
    public String toString() {
        return "FileStorageConfig{" +
                "basePath='" + basePath + '\'' +
                ", urlPrefix='" + urlPrefix + '\'' +
                ", allowedTypes=" + allowedTypes +
                '}';
    }

    @PostConstruct
    public void init() {
        if (baseUrl.isEmpty()) {
            try {
                // 自动获取服务器地址
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                String port = System.getProperty("server.port", "8080");
                String contextPath = System.getProperty("server.servlet.context-path", "/api");

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
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String prefix = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
        String path = relativePath.startsWith("/") ? relativePath : "/" + relativePath;

        return base + prefix + path;
    }

    // 新增 getter 和 setter
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


}