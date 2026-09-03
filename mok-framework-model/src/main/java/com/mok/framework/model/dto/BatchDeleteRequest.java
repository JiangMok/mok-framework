package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BatchDeleteRequest {
    @NotEmpty(message = "文件ID列表不能为空")
    @Size(max = 100, message = "单次最多删除100个文件")
    private List<@NotBlank(message = "文件ID不能为空") String> ids;

    // 无参构造函数
    public BatchDeleteRequest() {
    }

    // 有参构造函数
    public BatchDeleteRequest(List<String> ids) {
        this.ids = ids;
    }

    // Getter
    public List<String> getIds() {
        return ids;
    }

    // Setter
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    // toString 方法
    @Override
    public String toString() {
        return "BatchDeleteRequest{" +
                "ids=" + ids +
                '}';
    }

    // equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BatchDeleteRequest that = (BatchDeleteRequest) o;
        return ids != null ? ids.equals(that.ids) : that.ids == null;
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return ids != null ? ids.hashCode() : 0;
    }
}
