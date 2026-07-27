package com.mok.framework.mail.controller;

import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.mail.service.MailSenderService;
import com.mok.framework.model.dto.MailSenderDTO;
import com.mok.framework.model.entity.MailSender;
import com.mok.framework.ratelimiter.annotation.PreventDuplicate;
import com.mok.framework.ratelimiter.annotation.RateLimit;
import com.mok.framework.ratelimiter.enums.RateLimitScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 发件箱配置 Controller
 *
 * @author mok
 * @date 2026/7/17
 */
@RestController
@RequestMapping("/mail-sender")
@Tag(name = "发件箱配置", description = "系统邮箱配置管理")
public class MailSenderController {

    private final MailSenderService mailSenderService;

    public MailSenderController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @Operation(summary = "获取发件箱配置")
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @GetMapping
    public R<MailSender> getConfig() {
        return R.ok(mailSenderService.getConfig());
    }

    @Operation(summary = "更新发件箱配置（热刷新，无需重启）")
    @OperationLog(title = "发件箱配置", businessType = BusinessType.UPDATE)
    @RateLimit(scope = RateLimitScope.USER, limit = 20)
    @PreventDuplicate(lockTime = 3, message = "请勿重复提交")
    @PutMapping
    public R<Void> updateConfig(@Valid @RequestBody MailSenderDTO dto) {
        mailSenderService.updateConfig(dto);
        return R.ok();
    }
}
