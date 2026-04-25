package com.mok.framework.operationLog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.operationLog.service.OperationLogService;
import com.mok.framework.model.entity.OperationLogEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * ElasticSearch 版本操作日志controller
 *
 * @author: mok
 * @date: 2026/3/27
 */
@RestController
@RequestMapping("/operation-log")
@Tag(name = "ElasticSearch操作日志", description = "ElasticSearch操作日志相关接口")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Operation(summary = "分页查询操作日志")
    @OperationLog(title = "分页查询操作日志", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @SaCheckPermission("system:log:query")
    public R<PageResult<OperationLogEntity>> page(@RequestBody PageParam param) {
        return R.ok(operationLogService.getPageList(param));
    }

    /**
     * @description: 清理某一日期之前的日志
     * @author: JN
     * @date: 2026/1/6 14:21
     * @param: [beforeDate]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "清理历史日志")
    @OperationLog(title = "清除历史日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    @SaCheckPermission("system:log:delete")
    public R<String> cleanLogs(
            @Parameter(description = "清理指定日期之前的日志，格式：yyyy/MM/dd HH:mm:ss")
            @RequestParam("beforeDate") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss") LocalDateTime beforeDate) {
        int deletedCount = operationLogService.cleanLogsBefore(beforeDate);
        return R.ok(String.format("已清理%d条日志", deletedCount));
    }

    /**
     * @description: 通过ID删除某一条日志
     * @author: mok
     * @date: 2026/3/29 15:26
     * @param: [id]
     * @return: com.mok.baseframe.common.R<java.lang.String>
     **/
    @Operation(summary = "清理一条日志")
    @OperationLog(title = "清理一条日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:log:delete")
    public R<String> deleteById(@PathVariable("id") String id) {
        operationLogService.deleteById(id);
        return R.ok("删除成功");
    }
}
