package com.mok.baseframe.base.controller;

import com.mok.baseframe.base.service.PermissionService;
import com.mok.baseframe.base.service.RoleService;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.common.annotation.OperationLog;
import com.mok.baseframe.dto.RoleDTO;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.entity.RoleEntity;
import com.mok.baseframe.common.enums.BusinessType;
import com.mok.baseframe.common.utils.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description: 角色管理 controller
 * @author: JN
 * @date: 2026/1/5 16:57
 * @param:
 * @return:
 **/

@RestController
@RequestMapping("/role")

@Tag(name = "角色管理", description = "角色相关接口")
public class RoleController {
    private static final Logger log = LogUtils.getLogger(RoleController.class);

    private final RoleService roleService;
    private final PermissionService permissionService;

    public RoleController(RoleService roleService,
                          PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    /**
     * @description: 分页查询角色列表
     * @author: JN
     * @date: 2026/1/5 16:50
     * @param: [param]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.common.PageResult < com.mok.securityframework.entity.Role>>
     **/
    @Operation(summary = "分页查询角色列表")
    @OperationLog(title = "分页查询角色", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:query')")
    public R<PageResult<RoleEntity>> page(@RequestBody @Valid PageParam param) {
        return R.ok(roleService.getPageList(param));
    }

    /**
     * @description: 获取所有可用角色
     * @author: JN
     * @date: 2026/1/5 16:50
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.List < com.mok.securityframework.entity.Role>>
     **/
    @Operation(summary = "获取所有可用角色")
    @OperationLog(title = "获取所有可用角色", businessType = BusinessType.QUERY)
    @GetMapping("/all")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:query')")
    public R<List<RoleEntity>> getAllRoles() {
        return R.ok(roleService.getAllActiveRoles());
    }

    /**
     * @description: 根据 id 获取角色详情
     * @author: JN
     * @date: 2026/1/5 16:50
     * @param: [id]
     * @return: com.mok.securityframework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "通过 id 获取角色详情")
    @OperationLog(title = "通过ID获取角色详情", businessType = BusinessType.QUERY)
    @GetMapping("/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:query')")
    public R<Map<String, Object>> getRoleDetail(
            @Parameter(description = "角色ID") @PathVariable("id") String id) {
        if (id == null || id.trim().isEmpty()) {
            return R.error(400, "角色ID不能为空");
        }
        RoleEntity roleEntity = roleService.getById(id);
        if (roleEntity == null) {
            return R.error(404, "角色不存在");
        }
        List<String> permissionIdList = roleService.getRolePermissionIds(id);
        List<PermissionEntity> permissionList = roleService.getRolePermissions(id);

        Map<String, Object> result = Map.of(
                "role", roleEntity,
                "permissionIds", permissionIdList,
                "permissions", permissionList
        );

        return R.ok(result);
    }

    /**
     * @description: 创建角色
     * @author: JN
     * @date: 2026/1/5 16:51
     * @param: [roleDTO]
     * @return: com.mok.securityframework.common.R<java.lang.Long>
     **/
    @Operation(summary = "创建角色")
    @OperationLog(title = "创建角色", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:add')")
    public R<String> createRole(@RequestBody @Valid RoleDTO roleDTO) {
        //todo 暂时设置为由"超级管理员"角色创建
        roleDTO.setCreateBy("1");
        String roleId = roleService.createRole(roleDTO);
        return R.ok("创建成功", roleId);
    }

    /**
     * @description: 更新角色
     * @author: JN
     * @date: 2026/1/5 16:53
     * @param: [roleDTO]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "更新角色")
    @OperationLog(title = "更新角色", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:edit')")
    public R<String> updateRole(@RequestBody @Valid RoleDTO roleDTO) {
        if (roleService.updateRole(roleDTO)) {
            return R.ok("更新成功");
        }
        return R.error("更新失败");
    }

    /**
     * @description: 删除角色
     * @author: JN
     * @date: 2026/1/5 16:53
     * @param: [id]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "删除角色")
    @OperationLog(title = "删除角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:delete')")
    public R<String> deleteRole(@Parameter(description = "角色ID") @PathVariable("id") String id) {
        if (roleService.deleteRole(id)) {
            return R.ok("删除成功");
        }
        return R.error("删除失败");
    }

    /**
     * @description: 修改角色状态
     * @author: JN
     * @date: 2026/1/5 16:55
     * @param: [id, status]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "修改角色状态")
    @OperationLog(title = "修改角色状态", businessType = BusinessType.DELETE)
    @PutMapping("/{id}/status")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:edit')")
    public R<String> updateStatus(
            @Parameter(description = "角色ID") @PathVariable("id") String id,
            @Parameter(description = "状态：0-禁用，1-正常") @RequestParam("status") Integer status) {

        if (status != 0 && status != 1) {
            return R.error(400, "状态值不正确");
        }

        RoleEntity roleEntity = roleService.getById(id);
        if (roleEntity == null) {
            return R.error(404, "角色不存在");
        }

        // 修改：添加业务校验，防止禁用某些关键角色
        if ("ROLE_ADMIN".equals(roleEntity.getRoleCode())) {
            return R.error(403, "不能修改超级管理员角色的状态");
        }
        roleEntity.setStatus(status);
        roleService.updateById(roleEntity);

        String statusText = status == 1 ? "启用" : "禁用";
        log.info("修改角色状态：{} -> {}", roleEntity.getRoleName(), statusText);
        return R.ok("状态修改成功");
    }

    /**
     * @description: 获取权限树
     * @author: JN
     * @date: 2026/1/5 16:56
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.List < java.util.Map < java.lang.String, java.lang.Object>>>
     **/
    @Operation(summary = "获取权限树")
    @OperationLog(title = "获取权限树", businessType = BusinessType.QUERY)
    @GetMapping("/permission-tree")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:query')")
    public R<List<Map<String, Object>>> getPermissionTree() {
        List<Map<String, Object>> permissionTree = permissionService.getPermissionTree();
        return R.ok(permissionTree);
    }

    /**
     * @description: 分配角色权限
     * @author: JN
     * @date: 2026/1/5 16:56
     * @param: [id, permissionIds]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "分配角色权限")
    @OperationLog(title = "分配角色权限", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/permissions")
    @PreAuthorize("@permissionChecker.hasPermission('system:role:edit')")
    public R<String> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable("id") String id,
            @RequestBody List<String> permissionIds) {
        roleService.assignRolePermissions(id, permissionIds);
        return R.ok("权限分配成功");
    }

    /**
     * @description: 获取角色用户列表
     * @author: JN
     * @date: 2026/1/5 16:56
     * @param: [userId]
     * @return: com.mok.securityframework.common.R<java.util.List < com.mok.securityframework.entity.Role>>
     **/
    @Operation(summary = "获取用户角色列表")
    @OperationLog(title = "获取用户角色列表", businessType = BusinessType.QUERY)
    @GetMapping("/user/{userId}")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:query')")
    public R<List<RoleEntity>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable("userId") String userId) {

        List<RoleEntity> roleEntities = roleService.getRolesByUserId(userId);
        return R.ok(roleEntities);
    }
}