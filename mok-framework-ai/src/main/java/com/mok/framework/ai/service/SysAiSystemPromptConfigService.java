package com.mok.framework.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.model.dto.AiSystemPromptConfigDTO;
import com.mok.framework.model.entity.SysAiSystemPromptConfig;
import com.mok.framework.model.enums.AiAnalysisRequestType;

/**
 * AI系统提示词配置 Service
 *
 * @author mok
 * @date 2026/7/21
 */
public interface SysAiSystemPromptConfigService {

    /**
     * 分页查询
     */
    Page<SysAiSystemPromptConfig> page(long pageNum, long pageSize);

    /**
     * 按 ID 查询
     */
    SysAiSystemPromptConfig getById(String id);

    /**
     * 新增
     */
    void create(AiSystemPromptConfigDTO dto);

    /**
     * 更新
     */
    void update(AiSystemPromptConfigDTO dto);

    /**
     * 删除
     */
    void delete(String id);
    
    /**
     * @description: 通过AI分析请求的类型获取对应的系统提示词
     * @author: mok
     * @date: 2026/7/21 11:08
     * @param: [aiAnalysisRequestType]
     * @return: com.mok.framework.model.entity.SysAiSystemPromptConfig
    **/
    SysAiSystemPromptConfig getByAiAnalysisRequestType(AiAnalysisRequestType aiAnalysisRequestType);
}
