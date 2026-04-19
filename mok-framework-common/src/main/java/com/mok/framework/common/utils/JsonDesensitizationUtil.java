package com.mok.framework.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON 数据脱敏工具类
 * 提供敏感字段的过滤和移除功能
 */
@Component
public class JsonDesensitizationUtil {
    private static final Logger log = LogUtils.getLogger(JsonDesensitizationUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 常量使用大写+下划线命名
    private static final List<String> DEFAULT_SENSITIVE_KEYS = Arrays.asList(
            "password", "token", "key", "secret", "pwd", "pass", "credential"
    );

    /**
     * 脱敏 JSON 字符串
     *
     * @param jsonStr JSON 字符串
     * @param sensitiveKeys 脱敏关键词列表
     * @return 脱敏后的 JSON 字符串
     */
    public String desensitizeJson(String jsonStr, String... sensitiveKeys) {
        if (!StringUtils.hasText(jsonStr)) {
            return jsonStr;
        }

        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonStr);
            List<String> keys = getSensitiveKeys(sensitiveKeys);
            JsonNode processedNode = desensitizeJsonNode(jsonNode, keys);
            return OBJECT_MAPPER.writeValueAsString(processedNode);
        } catch (Exception e) {
            log.error("JSON 脱敏失败: {}", e.getMessage());
            return jsonStr;
        }
    }

    /**
     * 脱敏 JSON 字符串（使用默认敏感词）
     */
    public String desensitizeJson(String jsonStr) {
        return desensitizeJson(jsonStr, new String[0]);
    }

    /**
     * 获取敏感关键词列表
     */
    private List<String> getSensitiveKeys(String... sensitiveKeys) {
        if (sensitiveKeys != null && sensitiveKeys.length > 0) {
            return Arrays.stream(sensitiveKeys)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }
        return DEFAULT_SENSITIVE_KEYS;
    }

    /**
     * 递归处理 JSON 节点
     */
    private JsonNode desensitizeJsonNode(JsonNode node, List<String> sensitiveKeys) {
        if (node == null || node.isNull()) {
            return node;
        }

        if (node.isObject()) {
            return desensitizeObjectNode((ObjectNode) node, sensitiveKeys);
        } else if (node.isArray()) {
            return desensitizeArrayNode((ArrayNode) node, sensitiveKeys);
        } else {
            return node;
        }
    }

    /**
     * 处理对象节点 - 使用未弃用的 API
     */
    private ObjectNode desensitizeObjectNode(ObjectNode objectNode, List<String> sensitiveKeys) {
        if (objectNode == null || objectNode.isEmpty()) {
            return objectNode;
        }

        ObjectNode resultNode = objectNode.deepCopy();
        List<String> fieldsToRemove = new ArrayList<>();

        // 使用未弃用的 API - 替代 fields()
        // 方法1: 使用 fieldNames() 和 get()
        Iterator<String> fieldNames = resultNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode value = resultNode.get(fieldName);

            if (isSensitiveField(fieldName, sensitiveKeys)) {
                fieldsToRemove.add(fieldName);
            } else {
                resultNode.set(fieldName, desensitizeJsonNode(value, sensitiveKeys));
            }
        }

        // 方法2: 使用 properties() (Jackson 2.13+ 推荐)
        // resultNode.properties().forEach((fieldName, value) -> {
        //     if (isSensitiveField(fieldName, sensitiveKeys)) {
        //         fieldsToRemove.add(fieldName);
        //     } else {
        //         resultNode.set(fieldName, desensitizeJsonNode(value, sensitiveKeys));
        //     }
        // });

        fieldsToRemove.forEach(resultNode::remove);
        return resultNode;
    }

    /**
     * 处理数组节点
     */
    private ArrayNode desensitizeArrayNode(ArrayNode arrayNode, List<String> sensitiveKeys) {
        if (arrayNode == null || arrayNode.isEmpty()) {
            return arrayNode;
        }

        ArrayNode resultNode = OBJECT_MAPPER.createArrayNode();
        for (JsonNode element : arrayNode) {
            resultNode.add(desensitizeJsonNode(element, sensitiveKeys));
        }
        return resultNode;
    }

    /**
     * 检查字段名是否敏感
     */
    private boolean isSensitiveField(String fieldName, List<String> sensitiveKeys) {
        if (!StringUtils.hasText(fieldName) || CollectionUtils.isEmpty(sensitiveKeys)) {
            return false;
        }

        String lowerFieldName = fieldName.toLowerCase();
        return sensitiveKeys.stream()
                .filter(StringUtils::hasText)
                .anyMatch(key -> lowerFieldName.contains(key.toLowerCase()));
    }

    /**
     * 脱敏对象
     */
    public <T> T desensitizeObject(T object, String... sensitiveKeys) {
        if (object == null) {
            return null;
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(object);
            String desensitizedJson = desensitizeJson(json, sensitiveKeys);

            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) object.getClass();
            return OBJECT_MAPPER.readValue(desensitizedJson, clazz);
        } catch (Exception e) {
            log.error("对象脱敏失败: {}", e.getMessage());
            return object;
        }
    }

    /**
     * 脱敏对象（类型安全版本）
     */
    public <T> T desensitizeObjectSafe(T object, Class<T> clazz, String... sensitiveKeys) {
        if (object == null) {
            return null;
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(object);
            String desensitizedJson = desensitizeJson(json, sensitiveKeys);
            return OBJECT_MAPPER.readValue(desensitizedJson, clazz);
        } catch (Exception e) {
            log.error("对象脱敏失败: {}", e.getMessage());
            return object;
        }
    }

    /**
     * 脱敏 Map
     */
    public Map<String, Object> desensitizeMap(Map<String, Object> map, String... sensitiveKeys) {
        if (CollectionUtils.isEmpty(map)) {
            return new HashMap<>(map);
        }

        List<String> keys = getSensitiveKeys(sensitiveKeys);
        Map<String, Object> result = new LinkedHashMap<>(map);
        List<String> keysToRemove = new ArrayList<>();

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            String key = entry.getKey();

            if (isSensitiveField(key, keys)) {
                keysToRemove.add(key);
            } else {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedMap = (Map<String, Object>) value;
                    result.put(key, desensitizeMap(nestedMap, keys.toArray(new String[0])));
                } else if (value instanceof List) {
                    result.put(key, desensitizeList((List<?>) value, keys));
                }
            }
        }

        keysToRemove.forEach(result::remove);
        return result;
    }

    /**
     * 脱敏 List
     */
    private List<Object> desensitizeList(List<?> list, List<String> sensitiveKeys) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>(list);
        }

        return list.stream()
                .map(item -> {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) item;
                        return desensitizeMap(map, sensitiveKeys.toArray(new String[0]));
                    } else if (item instanceof List) {
                        return desensitizeList((List<?>) item, sensitiveKeys);
                    } else {
                        return item;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 批量脱敏 JSON 字符串
     */
    public List<String> batchDesensitizeJson(List<String> jsonStrings, String... sensitiveKeys) {
        if (CollectionUtils.isEmpty(jsonStrings)) {
            return new ArrayList<>();
        }

        return jsonStrings.stream()
                .map(json -> this.desensitizeJson(json, sensitiveKeys))
                .collect(Collectors.toList());
    }

    /**
     * 批量脱敏对象
     */
    public <T> List<T> batchDesensitizeObject(List<T> objects, String... sensitiveKeys) {
        if (CollectionUtils.isEmpty(objects)) {
            return new ArrayList<>();
        }

        return objects.stream()
                .map(obj -> this.desensitizeObject(obj, sensitiveKeys))
                .collect(Collectors.toList());
    }

    /**
     * 从 JSON 字符串中直接移除敏感字段
     */
    public String removeSensitiveFields(String jsonStr, String... sensitiveKeys) {
        if (!StringUtils.hasText(jsonStr)) {
            return jsonStr;
        }

        try {
            // 使用 TypeReference 确保正确处理复杂类型
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
            Map<String, Object> map = OBJECT_MAPPER.readValue(jsonStr, typeRef);

            Map<String, Object> desensitized = desensitizeMap(map, sensitiveKeys);
            return OBJECT_MAPPER.writeValueAsString(desensitized);
        } catch (Exception e) {
            log.error("移除敏感字段失败: {}", e.getMessage());
            return jsonStr;
        }
    }
}