package com.mok.framework.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.base.service.DepartmentService;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.DepartmentDTO;
import com.mok.framework.model.entity.DepartmentEntity;
import com.mok.framework.ratelimiter.annotation.PreventDuplicate;
import com.mok.framework.ratelimiter.annotation.RateLimit;
import com.mok.framework.ratelimiter.enums.RateLimitScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 部门管理 Controller
 * @author: mok
 * @date: 2026/7/15
 **/
@RestController
@RequestMapping("/dept")
@Tag(name = "部门管理", description = "部门相关接口")
public class DepartmentController {

    private static final Logger log = LogUtils.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 获取部门树
     */
    @Operation(summary = "获取部门树")
    @OperationLog(title = "获取部门树", businessType = BusinessType.QUERY)
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @GetMapping("/tree")
    @SaCheckPermission("system:dept:query")
    public R<List<DepartmentDTO>> getDeptTree() {
        return R.ok(departmentService.getDeptTree());
    }

    /**
     * 获取当前用户部门范围内的部门树（非管理员只返回所在部门+子部门）
     */
    @Operation(summary = "获取部门范围树")
    @OperationLog(title = "获取部门范围树", businessType = BusinessType.QUERY)
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @GetMapping("/tree/scoped")
    @SaCheckPermission("system:dept:query")
    public R<List<DepartmentDTO>> getScopedDeptTree() {
        String userId = StpUtil.getLoginId().toString();
        return R.ok(departmentService.getScopedDeptTree(userId));
    }

    /**
     * 分页查询部门列表
     */
    @Operation(summary = "分页查询部门列表")
    @OperationLog(title = "分页查询部门", businessType = BusinessType.QUERY)
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @PostMapping("/list")
    @SaCheckPermission("system:dept:list")
    public R<PageResult<DepartmentEntity>> page(@RequestBody @Valid PageParam param) {
        return R.ok(departmentService.getPageList(param));
    }

    /**
     * 根据ID获取部门详情
     */
    @Operation(summary = "通过ID获取部门详情")
    @OperationLog(title = "通过ID获取部门详情", businessType = BusinessType.QUERY)
    @RateLimit(scope = RateLimitScope.USER, limit = 60)
    @GetMapping("/{id}")
    @SaCheckPermission("system:dept:query")
    public R<DepartmentEntity> getDeptDetail(
            @Parameter(description = "部门ID") @PathVariable("id") String id) {
        if (id == null || id.trim().isEmpty()) {
            return R.error(400, "部门ID不能为空");
        }
        DepartmentEntity entity = departmentService.getDeptById(id);
        if (entity == null) {
            return R.error(404, "部门不存在");
        }
        return R.ok(entity);
    }

    /**
     * 新增部门
     */
    @Operation(summary = "新增部门")
    @OperationLog(title = "新增部门", businessType = BusinessType.INSERT)
    @RateLimit(scope = RateLimitScope.USER, limit = 20)
    @PreventDuplicate(lockTime = 3, message = "请勿重复提交")
    @PostMapping("/add")
    @SaCheckPermission("system:dept:add")
    public R<String> createDept(@RequestBody @Valid DepartmentDTO departmentDTO) {
        String deptId = departmentService.createDept(departmentDTO);
        return R.ok("创建成功", deptId);
    }

    /**
     * 更新部门
     */
    @Operation(summary = "更新部门")
    @OperationLog(title = "更新部门", businessType = BusinessType.UPDATE)
    @RateLimit(scope = RateLimitScope.USER, limit = 20)
    @PreventDuplicate(lockTime = 3, message = "请勿重复提交")
    @PutMapping("/update")
    @SaCheckPermission("system:dept:edit")
    public R<String> updateDept(@RequestBody @Valid DepartmentDTO departmentDTO) {
        if (departmentDTO.getId() == null || departmentDTO.getId().trim().isEmpty()) {
            return R.error(400, "部门ID不能为空");
        }
        if (departmentService.updateDept(departmentDTO)) {
            return R.ok("更新成功");
        }
        return R.error("更新失败");
    }

    /**
     * 删除部门
     */
    @Operation(summary = "删除部门")
    @OperationLog(title = "删除部门", businessType = BusinessType.DELETE)
    @RateLimit(scope = RateLimitScope.USER, limit = 20)
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:dept:delete")
    public R<String> deleteDept(
            @Parameter(description = "部门ID") @PathVariable("id") String id) {
        if (departmentService.deleteDept(id)) {
            return R.ok("删除成功");
        }
        return R.error("删除失败");
    }
}
