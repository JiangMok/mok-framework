package com.mok.framework.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.ai.service.SysAiSystemPromptConfigService;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.model.dto.AiSystemPromptConfigDTO;
import com.mok.framework.model.entity.SysAiSystemPromptConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * AI系统提示词配置 Controller
 *
 * @author mok
 * @date 2026/7/21
 */
@RestController
@RequestMapping("/ai-system-prompt-config")
@Tag(name = "AI系统提示词配置", description = "AI系统提示词配置管理")
public class SysAiSystemPromptConfigController {

    private final SysAiSystemPromptConfigService service;

    public SysAiSystemPromptConfigController(SysAiSystemPromptConfigService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询")
    @OperationLog(title = "分页查询AI系统提示词配置", businessType = BusinessType.QUERY)
    @GetMapping("/page")
    public R<Page<SysAiSystemPromptConfig>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(service.page(pageNum, pageSize));
    }

    @Operation(summary = "按ID查询")
    @OperationLog(title = "查询AI系统提示词配置详情", businessType = BusinessType.QUERY)
    @GetMapping("/{id}")
    public R<SysAiSystemPromptConfig> getById(@PathVariable String id) {
        return R.ok(service.getById(id));
    }

    @Operation(summary = "新增")
    @OperationLog(title = "新增AI系统提示词配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> create(@Valid @RequestBody AiSystemPromptConfigDTO dto) {
        service.create(dto);
        return R.ok();
    }

    @Operation(summary = "更新")
    @OperationLog(title = "更新AI系统提示词配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody AiSystemPromptConfigDTO dto) {
        service.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @OperationLog(title = "删除AI系统提示词配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        service.delete(id);
        return R.ok();
    }
}
