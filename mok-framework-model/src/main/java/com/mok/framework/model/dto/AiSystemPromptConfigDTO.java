package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI系统提示词配置 DTO
 */
public class AiSystemPromptConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    @NotBlank(message = "AI分析类型不能为空")
    @Pattern(regexp = "OPERATION_LOG|MQ_FAILED_MESSAGE", message = "AI分析类型不合法")
    private String aiAnalysisRequestType;

    @NotBlank(message = "系统提示词不能为空")
    private String systemPrompt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ---------- 构造方法 ----------
    public AiSystemPromptConfigDTO() {
    }

    public AiSystemPromptConfigDTO(String id, String aiAnalysisRequestType,
                                   String systemPrompt, LocalDateTime createTime,
                                   LocalDateTime updateTime) {
        this.id = id;
        this.aiAnalysisRequestType = aiAnalysisRequestType;
        this.systemPrompt = systemPrompt;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // ---------- Getter / Setter ----------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAiAnalysisRequestType() {
        return aiAnalysisRequestType;
    }

    public void setAiAnalysisRequestType(String aiAnalysisRequestType) {
        this.aiAnalysisRequestType = aiAnalysisRequestType;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    // ---------- equals / hashCode / toString ----------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiSystemPromptConfigDTO that = (AiSystemPromptConfigDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AiSystemPromptConfigDTO{" +
                "id='" + id + '\'' +
                ", aiAnalysisRequestType='" + aiAnalysisRequestType + '\'' +
                ", systemPrompt='" + systemPrompt + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
