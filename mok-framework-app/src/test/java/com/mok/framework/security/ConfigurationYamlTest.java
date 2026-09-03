package com.mok.framework.security;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationYamlTest {

    @Test
    void allApplicationYamlFilesAreValid() {
        for (String resource : new String[]{
                "application.yml",
                "application-dev.yml",
                "application-dev-no-es.yml",
                "application-prod.yml"}) {
            assertNotNull(load(resource), resource + " 无法解析");
        }
    }

    @Test
    void productionUsesMysqlOperationLogAndCurrentTomcatKeys() {
        Map<String, Object> root = load("application-prod.yml");
        Map<String, Object> mok = map(root.get("mok"));
        Map<String, Object> operationLog = map(mok.get("operation-log"));
        assertEquals("mysql", operationLog.get("save-location"));

        Map<String, Object> server = map(root.get("server"));
        Map<String, Object> tomcat = map(server.get("tomcat"));
        Map<String, Object> threads = map(tomcat.get("threads"));
        assertEquals(500, threads.get("max"));
        assertEquals(50, threads.get("min-spare"));
    }

    @Test
    void developmentUploadPathDoesNotUseUserHome() {
        Map<String, Object> root = load("application-dev-no-es.yml");
        Map<String, Object> file = map(root.get("file"));
        Map<String, Object> storage = map(file.get("storage"));
        String basePath = String.valueOf(storage.get("base-path"));
        assertTrue(basePath.contains("FILE_STORAGE_BASE_PATH"));
        assertTrue(basePath.endsWith("./uploads}"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> load(String resource) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(inputStream, resource + " 不存在");
            return new Yaml().load(inputStream);
        } catch (Exception exception) {
            throw new AssertionError(resource + " 解析失败", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}

