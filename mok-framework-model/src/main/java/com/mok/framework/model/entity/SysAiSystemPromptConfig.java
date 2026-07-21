package com.mok.framework.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI系统提示词配置表实体
 */
@TableName("sys_ai_system_prompt_config")
public class SysAiSystemPromptConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private String id;

    /**
     * AI分析请求类型（对应 AiAnalysisRequestType 枚举的 code）
     */
    private String aiAnalysisRequestType;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建人名称（非数据库字段，查询时回填）
     */
    @TableField(exist = false)
    private String createByName;

    // ---------- 构造方法 ----------
    public SysAiSystemPromptConfig() {
    }

    public SysAiSystemPromptConfig(String id, String aiAnalysisRequestType, 
                                   String systemPrompt, LocalDateTime createTime,
                                   LocalDateTime updateTime, String createBy) {
        this.id = id;
        this.aiAnalysisRequestType = aiAnalysisRequestType;
        this.systemPrompt = systemPrompt;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createBy = createBy;
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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateByName() {
        return createByName;
    }

    public void setCreateByName(String createByName) {
        this.createByName = createByName;
    }

    // ---------- equals / hashCode / toString ----------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysAiSystemPromptConfig that = (SysAiSystemPromptConfig) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SysAiSystemPromptConfig{" +
                "id='" + id + '\'' +
                ", aiAnalysisRequestType='" + aiAnalysisRequestType + '\'' +
                ", systemPrompt='" + systemPrompt + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createBy='" + createBy + '\'' +
                ", createByName='" + createByName + '\'' +
                '}';
    }
}