package com.mok.framework.mail.controller;

import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.mail.service.MailRecipientService;
import com.mok.framework.model.dto.MailRecipientDTO;
import com.mok.framework.model.entity.MailRecipient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 收件人管理 Controller
 *
 * @author mok
 * @date 2026/7/17
 */
@RestController
@RequestMapping("/mail-recipient")
@Tag(name = "收件人管理", description = "收件人增删改查及测试发送")
public class MailRecipientController {

    private final MailRecipientService mailRecipientService;

    public MailRecipientController(MailRecipientService mailRecipientService) {
        this.mailRecipientService = mailRecipientService;
    }

    @Operation(summary = "分页查询收件人")
    @PostMapping("/page")
    public R<PageResult<MailRecipient>> page(@RequestBody PageParam param) {
        return R.ok(mailRecipientService.getPage(param));
    }

    @Operation(summary = "根据ID查询收件人")
    @GetMapping("/{id}")
    public R<MailRecipient> getById(@PathVariable String id) {
        return R.ok(mailRecipientService.getById(id));
    }

    @Operation(summary = "新增收件人")
    @OperationLog(title = "收件人管理-新增", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> create(@Valid @RequestBody MailRecipientDTO dto) {
        mailRecipientService.create(dto);
        return R.ok();
    }

    @Operation(summary = "更新收件人")
    @OperationLog(title = "收件人管理-更新", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody MailRecipientDTO dto) {
        mailRecipientService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除收件人")
    @OperationLog(title = "收件人管理-删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        mailRecipientService.delete(id);
        return R.ok();
    }

    @Operation(summary = "测试发送邮件")
    @OperationLog(title = "收件人管理-测试发送", businessType = BusinessType.OTHER)
    @PostMapping("/test/{id}")
    public R<Void> testSend(@PathVariable String id) {
        mailRecipientService.testSend(id);
        return R.ok();
    }
}
