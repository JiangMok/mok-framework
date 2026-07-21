package com.mok.framework.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.SysAiSystemPromptConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI系统提示词配置 Mapper
 *
 * @author mok
 * @date 2026/7/21
 */
@Mapper
public interface SysAiSystemPromptConfigMapper extends BaseMapper<SysAiSystemPromptConfig> {

    SysAiSystemPromptConfig getByAiAnalysisRequestType(@Param("type") String aiAnalysisRequestType);
}
