package com.mok.framework.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mok.framework.model.enums.AiAnalysisRequestType;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * @description: AI 分析请求 DTO
 * @author: JN
 * @date: 2026/7/21
 */
public class AiAnalysisRequest {

    private String id;

    //类型 使用枚举类
    @JsonProperty("type")
    private AiAnalysisRequestType aiAnalysisRequestType;

    public AiAnalysisRequest(String id, AiAnalysisRequestType aiAnalysisRequestType) {
        this.id = id;
        this.aiAnalysisRequestType = aiAnalysisRequestType;
    }

    public AiAnalysisRequestType getAiAnalysisRequestType() {
        return aiAnalysisRequestType;
    }

    public void setAiAnalysisRequestType(AiAnalysisRequestType aiAnalysisRequestType) {
        this.aiAnalysisRequestType = aiAnalysisRequestType;
    }



    // 默认构造函数
    public AiAnalysisRequest() {
    }
    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        AiAnalysisRequest that = (AiAnalysisRequest) o;
        return Objects.equals(id, that.id);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "AiAnalysisRequest{" +
                "id='" + id + '\'' +
                ", AiAnalysisRequestType='" + aiAnalysisRequestType + '\'' +
                '}';
    }

}
