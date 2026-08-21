package com.mok.framework.operationLog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import com.mok.framework.operationLog.dto.OperationLogQueryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.jiangmok.operationlog.annotation.OperationLog;
import top.jiangmok.operationlog.entity.OperationLogEntity;
import top.jiangmok.operationlog.enums.BusinessType;
import top.jiangmok.operationlog.model.OperationLogPageResult;
import top.jiangmok.operationlog.service.OperationLogService;
import top.jiangmok.ratelimiter.annotation.PreventDuplicate;
import top.jiangmok.ratelimiter.annotation.RateLimit;
import top.jiangmok.ratelimiter.enums.RateLimitScope;

import java.time.LocalDateTime;

/**
 * 操作日志后台管理接口。
 * <p>日志采集、异步处理和存储由操作日志 Starter 提供。</p>
 *
 * @author mok
 */
@RestController
@RequestMapping("/operation-log")
@Tag(name = "操作日志", description = "操作日志查询与清理接口")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Operation(summary = "分页查询操作日志")
    @OperationLog(
            title = "分页查询操作日志",
            businessType = BusinessType.QUERY,
            saveResponseData = false)
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @PostMapping("/page")
    @SaCheckPermission("system:log:query")
    public R<PageResult<OperationLogEntity>> page(
            @Valid @RequestBody OperationLogQueryRequest request) {
        OperationLogPageResult result = operationLogService.pageQueryResult(
                request.getPageNum(),
                request.getPageSize(),
                request.getKeyword(),
                request.toConditions());
        return R.ok(PageResult.success(
                result.getRecords(),
                result.getTotal(),
                result.getPageNum(),
                result.getPageSize()));
    }

    @Operation(summary = "查询操作日志详情")
    @OperationLog(
            title = "查询操作日志详情",
            businessType = BusinessType.QUERY,
            saveResponseData = false)
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @GetMapping("/{id}")
    @SaCheckPermission("system:log:query")
    public R<OperationLogEntity> detail(@PathVariable String id) {
        return R.ok(operationLogService.findById(id));
    }

    @Operation(summary = "清理历史日志")
    @OperationLog(title = "清除历史日志", businessType = BusinessType.DELETE)
    @RateLimit(scope = RateLimitScope.USER, limit = 10, message = "清理操作过于频繁，请稍后重试")
    @PreventDuplicate(lockTime = 5, message = "请勿重复执行清理操作")
    @DeleteMapping("/clean")
    @SaCheckPermission("system:log:delete")
    public R<String> cleanLogs(
            @Parameter(description = "清理指定日期之前的日志，格式：yyyy/MM/dd HH:mm:ss")
            @RequestParam("beforeDate")
            @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss") LocalDateTime beforeDate) {
        int deletedCount = operationLogService.cleanLogsBefore(beforeDate);
        return R.ok(String.format("已清理%d条日志", deletedCount));
    }

    @Operation(summary = "删除一条操作日志")
    @OperationLog(title = "删除一条操作日志", businessType = BusinessType.DELETE)
    @RateLimit(scope = RateLimitScope.USER, limit = 20)
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:log:delete")
    public R<String> deleteById(@PathVariable String id) {
        operationLogService.deleteById(id);
        return R.ok("删除成功");
    }
}
