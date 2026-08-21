package com.mok.framework.mail.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import top.jiangmok.operationlog.annotation.OperationLog;
import top.jiangmok.operationlog.enums.BusinessType;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.model.entity.MailLog;
import top.jiangmok.ratelimiter.annotation.RateLimit;
import top.jiangmok.ratelimiter.enums.RateLimitScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件日志管理 Controller
 *
 * @author mok
 * @date 2026/7/18
 */
@RestController
@RequestMapping("/mail-log")
@Tag(name = "邮件日志管理", description = "邮件发送日志查询与删除")
public class MailLogController {

    private final MailLogService mailLogService;

    public MailLogController(MailLogService mailLogService) {
        this.mailLogService = mailLogService;
    }

    @Operation(summary = "分页查询邮件日志")
    @OperationLog(title = "邮件日志管理", businessType = BusinessType.QUERY)
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @PostMapping("/page")
    @SaCheckPermission("system:mailLog:query")
    public R<PageResult<MailLog>> page(@RequestBody PageParam param) {
        return R.ok(mailLogService.getPage(param));
    }

    @Operation(summary = "根据ID查询邮件日志详情")
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @GetMapping("/{id}")
    @SaCheckPermission("system:mailLog:query")
    public R<MailLog> getById(@PathVariable String id) {
        return R.ok(mailLogService.getById(id));
    }

    @Operation(summary = "删除邮件日志")
    @OperationLog(title = "邮件日志管理", businessType = BusinessType.DELETE)
    @RateLimit(scope = RateLimitScope.USER, limit = 20)
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:mailLog:delete")
    public R<Void> delete(@PathVariable String id) {
        mailLogService.deleteById(id);
        return R.ok();
    }
}
