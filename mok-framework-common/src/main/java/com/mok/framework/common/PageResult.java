package com.mok.framework.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 分页查询结果
 */
@Schema(description = "分页查询结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码", example = "200")
    private Integer code = 200;

    @Schema(description = "返回消息", example = "操作成功")
    private String message = "操作成功";

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "每页大小")
    private Integer pageSize;

    @Schema(description = "当前页码")
    private Integer pageNum;

    @Schema(description = "总页数")
    private Integer totalPages;

    @Schema(description = "是否有下一页")
    private Boolean hasNext;

    @Schema(description = "是否有上一页")
    private Boolean hasPrevious;

    @Schema(description = "数据列表")
    private List<T> data;

    // ============== 构造函数 ==============

    public PageResult() {
    }

    public PageResult(List<T> data, Long total, Integer pageNum, Integer pageSize) {
        this.data = data;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        calculate();
    }

    // ============== Builder 模式 ==============

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Integer code = 200;
        private String message = "操作成功";
        private Long total;
        private Integer pageSize;
        private Integer pageNum;
        private List<T> data;

        public Builder<T> code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> total(Long total) {
            this.total = total;
            return this;
        }

        public Builder<T> pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<T> pageNum(Integer pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public PageResult<T> build() {
            PageResult<T> result = new PageResult<>(data, total, pageNum, pageSize);
            result.setCode(code);
            result.setMessage(message);
            return result;
        }
    }

    // ============== Getter 和 Setter 方法 ==============

    public Integer getCode() {
        return code;
    }

    public PageResult<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public PageResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public Long getTotal() {
        return total;
    }

    public PageResult<T> setTotal(Long total) {
        this.total = total;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public PageResult<T> setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public PageResult<T> setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public PageResult<T> setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public PageResult<T> setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
        return this;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public PageResult<T> setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public PageResult<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    // ============== 计算方法 ==============

    private void calculate() {
        if (pageSize != null && pageSize > 0 && total != null) {
            this.totalPages = (int) Math.ceil((double) total / pageSize);
            this.hasNext = pageNum < totalPages;
            this.hasPrevious = pageNum > 1;
        } else {
            this.totalPages = 0;
            this.hasNext = false;
            this.hasPrevious = false;
        }
    }

    // ============== 静态工厂方法 ==============

    /**
     * 从MyBatis Plus的IPage创建PageResult
     */
    public static <T> PageResult<T> fromIPage(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setData(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageSize((int) page.getSize());
        result.setPageNum((int) page.getCurrent());
        result.calculate();
        return result;
    }

    /**
     * 从Page创建PageResult
     */
    public static <T> PageResult<T> fromPage(Page<T> page) {
        return fromIPage(page);
    }

    /**
     * 从列表创建PageResult（不分页的情况）
     */
    public static <T> PageResult<T> fromList(List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setData(list);
        result.setTotal((long) list.size());
        result.setPageSize(list.size());
        result.setPageNum(1);
        result.calculate();
        return result;
    }

    /**
     * 创建成功的分页结果
     */
    public static <T> PageResult<T> success(List<T> data, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(data, total, pageNum, pageSize);
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty(PageParam param) {
        PageResult<T> result = new PageResult<>();
        result.setData(List.of());
        result.setTotal(0L);
        result.setPageNum(param.getPageNum());
        result.setPageSize(param.getPageSize());
        result.calculate();
        return result;
    }

    /**
     * 创建分页结果（带自定义消息）
     */
    public static <T> PageResult<T> of(Integer code, String message, List<T> data, Long total,
                                       Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>(data, total, pageNum, pageSize);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // ============== 便捷方法 ==============

    /**
     * 获取起始索引
     */
    public Integer getStartIndex() {
        if (pageNum == null || pageSize == null || pageNum <= 1) {
            return 0;
        }
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取结束索引
     */
    public Integer getEndIndex() {
        if (pageNum == null || pageSize == null) {
            return 0;
        }
        return pageNum * pageSize;
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    /**
     * 获取数据条数
     */
    public Integer getSize() {
        return data != null ? data.size() : 0;
    }

    // ============== equals 和 hashCode ==============

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PageResult<?> that = (PageResult<?>) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(message, that.message) &&
                Objects.equals(total, that.total) &&
                Objects.equals(pageSize, that.pageSize) &&
                Objects.equals(pageNum, that.pageNum) &&
                Objects.equals(totalPages, that.totalPages) &&
                Objects.equals(hasNext, that.hasNext) &&
                Objects.equals(hasPrevious, that.hasPrevious) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, total, pageSize, pageNum, totalPages, hasNext, hasPrevious, data);
    }

    // ============== toString 方法 ==============

    @Override
    public String toString() {
        return "PageResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", total=" + total +
                ", pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                ", dataSize=" + (data != null ? data.size() : 0) +
                '}';
    }

    /**
     * 从 Spring Data Page 创建 PageResult
     * @param page Spring Data Page 对象
     * @param <T> 数据类型
     * @return PageResult
     */
    public static <T> PageResult<T> fromSpringDataPage(org.springframework.data.domain.Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setData(page.getContent());
        result.setTotal(page.getTotalElements());
        result.setPageSize(page.getSize());
        // Spring Data 页码从0开始，转为1
        result.setPageNum(page.getNumber() + 1);
        // 自动计算 totalPages、hasNext、hasPrevious
        result.calculate();
        return result;
    }
}