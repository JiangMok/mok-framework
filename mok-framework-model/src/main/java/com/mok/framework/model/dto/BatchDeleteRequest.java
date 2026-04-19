package com.mok.framework.model.dto;

import java.util.List;

public class BatchDeleteRequest {
    private List<String> ids;

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