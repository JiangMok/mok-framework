package com.mok.framework.mq.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.mq.service.MqFailedMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MQ失败消息管理 Controller
 *
 * @author mok
 * @date 2026/7/18
 */
@RestController
@RequestMapping("/mq-failed-message")
@Tag(name = "MQ失败消息管理", description = "MQ死信失败消息的查询、处理与删除")
public class MqFailedMessageController {

    private final MqFailedMessageService mqFailedMessageService;

    public MqFailedMessageController(MqFailedMessageService mqFailedMessageService) {
        this.mqFailedMessageService = mqFailedMessageService;
    }

    @Operation(summary = "分页查询MQ失败消息")
    @OperationLog(title = "MQ失败消息管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @SaCheckPermission("system:mqFailedMessage:query")
    public R<PageResult<MqFailedMessage>> page(@RequestBody PageParam param) {
        return R.ok(mqFailedMessageService.getPage(param));
    }

    @Operation(summary = "根据ID查询MQ失败消息详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:mqFailedMessage:query")
    public R<MqFailedMessage> getById(@PathVariable String id) {
        return R.ok(mqFailedMessageService.getById(id));
    }

    @Operation(summary = "删除MQ失败消息")
    @OperationLog(title = "MQ失败消息管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:mqFailedMessage:delete")
    public R<Void> delete(@PathVariable String id) {
        mqFailedMessageService.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "标记MQ失败消息为已处理")
    @OperationLog(title = "MQ失败消息管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/resolve")
    @SaCheckPermission("system:mqFailedMessage:edit")
    public R<Void> resolve(
            @PathVariable String id,
            @Parameter(description = "处理信息，包含 resolvedBy 和 remark")
            @RequestBody Map<String, String> body) {
        String resolvedBy = body.getOrDefault("resolvedBy", "admin");
        String remark = body.getOrDefault("remark", "");
        mqFailedMessageService.resolve(id, resolvedBy, remark);
        return R.ok();
    }
}
