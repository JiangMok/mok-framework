package com.mok.baseframe.base.controller;

import com.mok.baseframe.base.service.PermissionService;
import com.mok.baseframe.base.service.UserService;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.common.annotation.OperationLog;
import com.mok.baseframe.dto.PermissionDTO;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.common.enums.BusinessType;
import com.mok.baseframe.ratelimiter.annotation.PreventDuplicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description: 权限管理controller
 * @author: JN
 * @date: 2026/1/5 16:57
 * @param:
 * @return:
 **/
@RestController
@RequestMapping("/permission")
@Tag(name = "权限管理", description = "权限相关接口")
public class PermissionController {

    private final PermissionService permissionService;
    private final UserService userService;

    // 构造函数注入
    public PermissionController(PermissionService permissionService,
                                UserService userService) {
        this.permissionService = permissionService;
        this.userService = userService;
    }

    @Operation(summary = "分页查询权限列表")
    @OperationLog(title = "分页查询权限", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<PageResult<PermissionEntity>> page(@RequestBody @Valid PageParam param) {
        return R.ok(permissionService.getPageList(param));
    }

    /**
     * @description: 获取权限树
     * @author: JN
     * @date: 2026/1/6 14:21
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.List < java.util.Map < java.lang.String, java.lang.Object>>>
     **/
    @Operation(summary = "获取权限树")
    @GetMapping("/tree")
    @OperationLog(title = "获取权限树", businessType = BusinessType.QUERY)
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<List<Map<String, Object>>> getPermissionTree() {
        List<Map<String, Object>> permissionTree = permissionService.getPermissionTree();
        return R.ok(permissionTree);
    }

    /**
     * @description: 获取菜单树
     * @author: JN
     * @date: 2026/1/6 14:21
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.List < java.util.Map < java.lang.String, java.lang.Object>>>
     **/
    @Operation(summary = "获取菜单树")
    @OperationLog(title = "获取菜单树", businessType = BusinessType.QUERY)
    @GetMapping("/menu-tree")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<List<Map<String, Object>>> getMenuTree() {
        List<Map<String, Object>> menuTree = permissionService.getMenuTree();
        return R.ok(menuTree);
    }

    /**
     * @description: 获取当前用户菜单
     * @author: JN
     * @date: 2026/1/6 14:21
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.List < java.util.Map < java.lang.String, java.lang.Object>>>
     **/
    @Operation(summary = "获取当前用户菜单")
    @OperationLog(title = "获取当前用户", businessType = BusinessType.QUERY)
    @GetMapping("/my-menus")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<List<Map<String, Object>>> getMyMenus() {
        List<Map<String, Object>> menus = permissionService.getMenuTreeByUserId(getUserId());
        return R.ok(menus);
    }

    /**
     * @description: 获取接口权限列表
     * @author: JN
     * @date: 2026/1/6 14:22
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.List < com.mok.securityframework.entity.Permission>>
     **/
    @Operation(summary = "获取接口权限列表")
    @OperationLog(title = "获取接口权限列表", businessType = BusinessType.QUERY)
    @GetMapping("/apis")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<List<PermissionEntity>> getApiPermissions() {
        // 修改：添加参数校验和空值处理
        String userId = getUserId();
        if (userId == null) {
            return R.error(401, "用户未登录或会话已过期");
        }
        List<PermissionEntity> apiPermissionEntities = permissionService.getApiPermissions(userId);
        return R.ok(apiPermissionEntities);
    }

    @Operation(summary = "获取权限列表")
    @OperationLog(title = "获取权限列表", businessType = BusinessType.QUERY)
    @GetMapping("/getByUserId")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<List<PermissionEntity>> getApiPermissionsByUserId() {
        // 修改：修复方法名歧义，获取用户权限列表而非API权限
        String userId = getUserId();
        if (userId == null) {
            return R.error(401, "用户未登录或会话已过期");
        }
        List<PermissionEntity> apiPermissionEntities = permissionService.getPermissionsByUserId(userId);
        return R.ok(apiPermissionEntities);
    }

    /**
     * @description: 获取权限详情
     * @author: JN
     * @date: 2026/1/6 14:22
     * @param: [id]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.entity.Permission>
     **/
    @Operation(summary = "获取权限详情")
    @OperationLog(title = "获取权限详情", businessType = BusinessType.QUERY)
    @GetMapping("/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<PermissionEntity> getPermissionDetail(
            @Parameter(description = "权限ID") @PathVariable("id") String id) {
        // 修改：添加参数校验
        if (id == null || id.trim().isEmpty()) {
            return R.error(400, "权限ID不能为空");
        }
        PermissionEntity permissionEntity = permissionService.getById(id);
        if (permissionEntity == null) {
            return R.error(404, "权限不存在");
        }

        return R.ok(permissionEntity);
    }

    /**
     * @description: 创建权限
     * @author: JN
     * @date: 2026/1/6 14:22
     * @param: [permissionDTO]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @PreventDuplicate(
            key = "#permissionDTO.permissionName",
            lockTime = 3,
            message = "请勿重复提交"
    )
    @Operation(summary = "创建权限")
    @OperationLog(title = "创建权限", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:add')")
    public R<String> createPermission(@RequestBody @Valid PermissionDTO permissionDTO) {
        String permissionId = permissionService.createPermission(permissionDTO);
        return R.ok("创建成功", permissionId);
    }

    /**
     * @description: 更新权限
     * @author: JN
     * @date: 2026/1/6 14:22
     * @param: [permissionDTO]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "更新权限")
    @OperationLog(title = "更新权限", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:edit')")
    public R<String> updatePermission(@RequestBody @Valid PermissionDTO permissionDTO) {
        permissionService.updatePermission(permissionDTO);
        return R.ok("更新成功");
    }

    /**
     * @description: 删除权限
     * @author: JN
     * @date: 2026/1/6 14:22
     * @param: [id]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "删除权限")
    @OperationLog(title = "删除权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:delete')")
    public R<String> deletePermission(
            @Parameter(description = "权限ID") @PathVariable("id") String id) {

        permissionService.deletePermission(id);
        return R.ok("删除成功");
    }

    /**
     * @description: 获取指定类型的权限
     * @author: JN
     * @date: 2026/1/6 14:22
     * @param: [type]
     * @return: com.mok.securityframework.common.R<java.util.List < com.mok.securityframework.entity.Permission>>
     **/
    @Operation(summary = "获取指定类型的权限")
    @OperationLog(title = "获取指定类型的权限", businessType = BusinessType.QUERY)
    @GetMapping("/type/{type}")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<List<PermissionEntity>> getPermissionsByType(
            @Parameter(description = "权限类型：1-菜单，2-按钮，3-接口") @PathVariable("type") Integer type) {

        if (type < 1 || type > 3) {
            return R.error(400, "权限类型不正确");
        }

        List<PermissionEntity> permissionEntities =
                permissionService.selectPermissionsByUserIdByType(type, getUserId());
        return R.ok(permissionEntities);
    }

    /**
     * @description: 根据角色id查询权限
     * @author: JN
     * @date: 2026/1/19 16:56
     * @param: [roleId]
     * @return: com.mok.baseframe.common.R<java.util.List < com.mok.baseframe.entity.PermissionEntity>>
     **/
    @Operation(summary = "通过角色ID获取权限")
    @OperationLog(title = "通过角色ID获取权限", businessType = BusinessType.QUERY)
    @GetMapping("/getByRoleId/{roleId}")
    @PreAuthorize("@permissionChecker.hasPermission('system:permission:query')")
    public R<List<PermissionEntity>> selectPermissionsByRoleId(@PathVariable("roleId") String roleId) {
        return R.ok(permissionService.selectPermissionsByRoleId(roleId));
    }

    /**
     * @description: 获取当前用户ID
     * @author: JN
     * @date: 2026/1/12 19:38
     * @param: []
     * @return: java.lang.String
     **/
    private String getUserId() {
        // 获取当前用户ID
        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();

        //从安全上下文中获取用户名
        String username = authentication.getName();

        //通过 username 查询用户信息
        UserEntity userEntity = userService.lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .eq(UserEntity::getIsDeleted, 0)
                .one();
        return userEntity.getId();
    }
}