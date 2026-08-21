package com.mok.framework.ai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.ai.mapper.AiUserMapper;
import com.mok.framework.ai.mapper.SysAiSystemPromptConfigMapper;
import com.mok.framework.ai.service.SysAiSystemPromptConfigService;
import com.mok.framework.common.BusinessException;
import com.mok.framework.model.dto.AiSystemPromptConfigDTO;
import com.mok.framework.model.entity.SysAiSystemPromptConfig;
import com.mok.framework.model.entity.UserEntity;
import com.mok.framework.model.enums.AiAnalysisRequestType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI系统提示词配置 Service 实现
 *
 * @author mok
 * @date 2026/7/21
 */
@Service
public class SysAiSystemPromptConfigServiceImpl implements SysAiSystemPromptConfigService {

    private final SysAiSystemPromptConfigMapper mapper;
    private final AiUserMapper userMapper;

    public SysAiSystemPromptConfigServiceImpl(SysAiSystemPromptConfigMapper mapper,
                                               AiUserMapper userMapper) {
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    @Override
    public Page<SysAiSystemPromptConfig> page(long pageNum, long pageSize) {
        Page<SysAiSystemPromptConfig> page = new Page<>(pageNum, pageSize);
        Page<SysAiSystemPromptConfig> result = mapper.selectPage(page, new LambdaQueryWrapper<SysAiSystemPromptConfig>()
                .orderByDesc(SysAiSystemPromptConfig::getUpdateTime));
        // 批量回填创建人名称
        fillCreateByName(result.getRecords());
        return result;
    }

    @Override
    public SysAiSystemPromptConfig getById(String id) {
        SysAiSystemPromptConfig entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("AI系统提示词配置不存在");
        }
        // 回填创建人名称
        fillCreateByName(Collections.singletonList(entity));
        return entity;
    }

    @Override
    @Transactional
    public void create(AiSystemPromptConfigDTO dto) {
        SysAiSystemPromptConfig entity = new SysAiSystemPromptConfig();
        entity.setId(IdUtil.simpleUUID());
        entity.setAiAnalysisRequestType(dto.getAiAnalysisRequestType());
        entity.setSystemPrompt(dto.getSystemPrompt());
        entity.setCreateBy(StpUtil.getLoginId().toString());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        mapper.insert(entity);
    }

    @Override
    @Transactional
    public void update(AiSystemPromptConfigDTO dto) {
        SysAiSystemPromptConfig entity = mapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("AI系统提示词配置不存在");
        }
        entity.setAiAnalysisRequestType(dto.getAiAnalysisRequestType());
        entity.setSystemPrompt(dto.getSystemPrompt());
        entity.setUpdateTime(LocalDateTime.now());
        // createBy 不允许更新，保持原值
        mapper.updateById(entity);
    }

    @Override
    @Transactional
    public void delete(String id) {
        SysAiSystemPromptConfig entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("AI系统提示词配置不存在");
        }
        mapper.deleteById(id);
    }

    @Override
    public SysAiSystemPromptConfig getByAiAnalysisRequestType(AiAnalysisRequestType aiAnalysisRequestType) {
        return mapper.getByAiAnalysisRequestType(aiAnalysisRequestType.getCode());
    }

    /**
     * 批量回填创建人名称
     */
    private void fillCreateByName(List<SysAiSystemPromptConfig> list) {
        if (list == null || list.isEmpty()) return;
        // 收集所有创建人ID
        Set<String> userIds = list.stream()
                .map(SysAiSystemPromptConfig::getCreateBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        // 批量查询用户
        Map<String, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername, (a, b) -> a));
        // 回填
        for (SysAiSystemPromptConfig config : list) {
            if (config.getCreateBy() != null) {
                config.setCreateByName(userNameMap.get(config.getCreateBy()));
            }
        }
    }
}
