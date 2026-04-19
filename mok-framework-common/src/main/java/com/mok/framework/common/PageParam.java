package com.mok.framework.common;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Schema(description = "分页查询参数")
public class PageParam implements Serializable  {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 1000;
    public static final String ASC = "asc";
    public static final String DESC = "desc";

    @Schema(description = "页码，从1开始", example = "1", required = true)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    @Schema(description = "每页大小", example = "10", required = true)
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @Schema(description = "排序字段", example = "createTime")
    private String orderBy;

    @Schema(description = "排序方向：asc-升序，desc-降序", example = "desc")
    private String order = DESC;

    @Schema(description = "关键词搜索，支持多字段模糊查询")
    private String keyword;

    @Schema(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @Schema(description = "状态筛选")
    private Integer status;

    @Schema(description = "其他扩展参数")
    private Map<String, Object> params = new HashMap<>();

    // ============== 构造函数 ==============
    public PageParam() {
    }

    public PageParam(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum != null ? pageNum : DEFAULT_PAGE_NUM;
        this.pageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
    }

    public PageParam(Integer pageNum, Integer pageSize, String orderBy, String order) {
        this.pageNum = pageNum != null ? pageNum : DEFAULT_PAGE_NUM;
        this.pageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
        this.orderBy = orderBy;
        this.order = order != null ? order : DESC;
    }

    // ============== Getter/Setter ==============
    public Integer getPageNum() {
        return pageNum;
    }

    public PageParam setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public PageParam setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public PageParam setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public PageParam setOrder(String order) {
        this.order = order;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public PageParam setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public PageParam setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public PageParam setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public PageParam setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public PageParam setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    // ============== 工具方法 ==============
    @JsonIgnore
    public Object get(String key) {
        return params != null ? params.get(key) : null;
    }

    public void set(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
    }

    @JsonIgnore
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    @JsonIgnore
    public Integer getInteger(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @JsonIgnore
    public Long getLong(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @JsonIgnore
    public Boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Integer) {
            return ((Integer) value) != 0;
        }
        return null;
    }

    public void remove(String key) {
        if (params != null) {
            params.remove(key);
        }
    }

    @JsonIgnore
    public boolean containsKey(String key) {
        return params != null && params.containsKey(key);
    }

    public void clearParams() {
        if (params != null) {
            params.clear();
        }
    }

    @JsonIgnore
    public PageParam validate() {
        if (pageNum == null || pageNum < 1) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        if (order != null) {
            order = order.toLowerCase();
            if (!ASC.equals(order) && !DESC.equals(order)) {
                order = DESC;
            }
        }
        return this;
    }

    @JsonIgnore
    public <T> Page<T> toPage() {
        return toPage(null);
    }

    @JsonIgnore
    public <T> Page<T> toPage(String defaultOrderBy) {
        validate();
        Page<T> page = new Page<>(pageNum, pageSize);

        if (StringUtils.hasText(orderBy)) {
            if (ASC.equalsIgnoreCase(order)) {
                page.addOrder(OrderItem.asc(orderBy));
            } else {
                page.addOrder(OrderItem.desc(orderBy));
            }
        } else if (StringUtils.hasText(defaultOrderBy)) {
            if (ASC.equalsIgnoreCase(order)) {
                page.addOrder(OrderItem.asc(defaultOrderBy));
            } else {
                page.addOrder(OrderItem.desc(defaultOrderBy));
            }
        }
        return page;
    }

    @JsonIgnore
    public <T> Page<T> toPageWithoutOrder() {
        validate();
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 转换为 Spring Data 的 Pageable 对象
     * @return Pageable
     */
    @JsonIgnore
    public Pageable toPageable() {
        // 处理排序
        Sort sort = null;
        if (StringUtils.hasText(orderBy)) {
            // 默认排序：createTime 降序
            String sortField = StringUtils.hasText(orderBy) ? orderBy : "createTime";
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }
        // 页码从0开始
        return sort != null
                ? PageRequest.of(pageNum - 1, pageSize, sort)
                : PageRequest.of(pageNum - 1, pageSize);
    }

    @JsonIgnore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        map.put("keyword", keyword);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("status", status);

        if (params != null && !params.isEmpty()) {
            map.putAll(params);
        }

        if (StringUtils.hasText(orderBy)) {
            map.put("orderBy", orderBy);
            map.put("order", order);
        }
        return map;
    }

    @JsonIgnore
    public Integer getOffset() {
        validate();
        return (pageNum - 1) * pageSize;
    }

    @JsonIgnore
    public Integer getStartRow() {
        return getOffset();
    }

    @JsonIgnore
    public Integer getEndRow() {
        validate();
        return pageNum * pageSize;
    }

    @JsonIgnore
    public boolean hasTimeRange() {
        return StringUtils.hasText(startTime) || StringUtils.hasText(endTime);
    }

    @JsonIgnore
    public boolean hasKeyword() {
        return StringUtils.hasText(keyword);
    }

    @JsonIgnore
    public boolean hasStatus() {
        return status != null;
    }

    @Override
    public String toString() {
        return "PageParam{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", orderBy='" + orderBy + '\'' +
                ", order='" + order + '\'' +
                ", keyword='" + keyword + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", status=" + status +
                ", params=" + params +
                '}';
    }

    // ============== 静态工厂方法 ==============
    public static PageParam ofDefault() {
        return new PageParam(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);
    }

    public static PageParam of(Integer pageNum, Integer pageSize) {
        return new PageParam(pageNum, pageSize);
    }

    public static PageParam of(Integer pageNum, Integer pageSize, String orderBy, String order) {
        return new PageParam(pageNum, pageSize, orderBy, order);
    }

    public static PageParam withKeyword(String keyword) {
        PageParam param = ofDefault();
        param.setKeyword(keyword);
        return param;
    }

    public static PageParam withTimeRange(String startTime, String endTime) {
        PageParam param = ofDefault();
        param.setStartTime(startTime);
        param.setEndTime(endTime);
        return param;
    }

    public static PageParam withStatus(Integer status) {
        PageParam param = ofDefault();
        param.setStatus(status);
        return param;
    }

    // Builder类
    public static class Builder {
        private final PageParam pageParam;

        private Builder() {
            pageParam = new PageParam();
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder pageNum(Integer pageNum) {
            pageParam.setPageNum(pageNum);
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            pageParam.setPageSize(pageSize);
            return this;
        }

        public Builder orderBy(String orderBy) {
            pageParam.setOrderBy(orderBy);
            return this;
        }

        public Builder order(String order) {
            pageParam.setOrder(order);
            return this;
        }

        public Builder keyword(String keyword) {
            pageParam.setKeyword(keyword);
            return this;
        }

        public Builder startTime(String startTime) {
            pageParam.setStartTime(startTime);
            return this;
        }

        public Builder endTime(String endTime) {
            pageParam.setEndTime(endTime);
            return this;
        }

        public Builder status(Integer status) {
            pageParam.setStatus(status);
            return this;
        }

        public Builder param(String key, Object value) {
            pageParam.set(key, value);
            return this;
        }

        public PageParam build() {
            return pageParam.validate();
        }
    }
}