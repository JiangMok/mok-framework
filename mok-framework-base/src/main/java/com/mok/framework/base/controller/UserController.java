package com.mok.baseframe.base.controller;

import cn.hutool.core.util.IdUtil;
import com.mok.baseframe.base.service.RoleService;
import com.mok.baseframe.base.service.UserService;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.common.constant.ResponseCode;
import com.mok.baseframe.common.annotation.OperationLog;
import com.mok.baseframe.dto.UserDTO;
import com.mok.baseframe.dto.UserUpdateDto;
import com.mok.baseframe.entity.RoleEntity;
import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.common.enums.BusinessType;
import com.mok.baseframe.common.utils.LogUtils;
import com.mok.baseframe.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:用户controller
 * @author: JN
 * @date: 2026/1/2
 */

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {
    private static final Logger log = LogUtils.getLogger(UserController.class);

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    public UserController(UserService userService,
                          RoleService roleService,
                          PasswordEncoder passwordEncoder,
                          SecurityUtils securityUtils) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    /**
     * @description: 分页查询用户信息
     * @author: JN
     * @date: 2026/1/2 16:16
     * @param: [param]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.common.PageResult < com.mok.securityframework.entity.User>>
     **/
    @Operation(summary = "分页查询用户信息")
    @OperationLog(title = "分页查询用户信息", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:list')")
    public R<PageResult<UserEntity>> page(@RequestBody @Valid PageParam param) {
        return R.ok(userService.getPageListWithPermission(param, true));
    }

    /**
     * @description: 通过用户ID查询用户信息
     * @author: JN
     * @date: 2026/1/2 16:20
     * @param: [id]
     * @return: com.mok.securityframework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "根据 id 查询用户信息")
    @OperationLog(title = "根据 id 查询用户信息", businessType = BusinessType.QUERY)
    @GetMapping("/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:query')")
    public R<Map<String, Object>> detail(@PathVariable("id") String id) {
        // 参数校验
        if (id == null || id.trim().isEmpty()) {
            return R.error(400, "用户ID不能为空");
        }
        // 数据权限检查
        if (!userService.canViewUser(id)) {
            log.warn("用户 {} 尝试访问无权限的用户信息: {}",
                    securityUtils.getCurrentUsername(), id);
            return R.forbidden("无权查看该用户信息");
        }
        UserEntity userEntity = userService.getById(id);
        if (userEntity == null) {
            return R.error(404, "用户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("user", userEntity);
        result.put("roleIds", roleService.getRolesByUserId(id).stream()
                .map(RoleEntity::getId)
                .toList());
        return R.ok(result);
    }

    /**
     * @description: 创建用户
     * @author: JN
     * @date: 2026/1/2 16:27
     * @param: [userDTO]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "创建用户")
    @OperationLog(title = "创建用户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:add')")
    public R<String> create(@RequestBody @Valid UserDTO userDTO) {
        Long count = userService.lambdaQuery()
                .eq(UserEntity::getUsername, userDTO.getUsername())
                .eq(UserEntity::getIsDeleted, 0)
                .count();
        if (count > 0) {
            return R.error(1001, "用户名已存在");
        }
        // 获取当前登录用户，设置创建者
        UserEntity currentUserEntity = securityUtils.getCurrentUser();
        if (currentUserEntity == null) {
            return R.error(401, "请先登录");
        }
        //构建用户信息
        UserEntity userEntity = new UserEntity();
        userEntity.setId(IdUtil.simpleUUID());
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userEntity.setNickname(userDTO.getNickname());
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setPhone(userDTO.getPhone());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setAvatar(userDTO.getAvatar());
        userEntity.setStatus(userDTO.getStatus());
        // 设置创建者为当前用户
        userEntity.setCreateBy(currentUserEntity.getId());
        userService.save(userEntity);
        //添加角色信息
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            roleService.assignUserRoles(userEntity.getId(), userDTO.getRoleIds());
        }

        log.info("创建用户成功:{}", userDTO.getUsername());
        return R.ok("创建用户成功");
    }

    /**
     * @description: 更新用户信息
     * @author: JN
     * @date: 2026/1/2 16:41
     * @param: [userDto]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "修改用户信息")
    @OperationLog(title = "修改用户信息", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:edit')")
    public R<String> update(@RequestBody @Valid UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getId() == null) {
            return R.error(400, "用户ID不能为空");
        }
        // ==================== 新增：数据权限检查 ====================
        if (!userService.canEditUser(userUpdateDto.getId())) {
            log.warn("用户 {} 尝试修改无权限的用户信息: {}",
                    securityUtils.getCurrentUsername(), userUpdateDto.getId());
            return R.forbidden("无权修改该用户信息");
        }
        UserEntity userEntity = userService.getById(userUpdateDto.getId());
        if (userEntity == null) {
            return R.error(404, "用户不存在");
        }
        Long count = userService.lambdaQuery()
                .eq(UserEntity::getUsername, userUpdateDto.getUsername())
                .ne(UserEntity::getId, userUpdateDto.getId())
                .count();
        if (count > 0) {
            return R.error(1001, "用户名已占用,请使用其他用户名");
        }

        // 更新用户信息
        userEntity.setUsername(userUpdateDto.getUsername());
        userEntity.setNickname(userUpdateDto.getNickname());
        userEntity.setPhone(userUpdateDto.getPhone());
        userEntity.setEmail(userUpdateDto.getEmail());
        userEntity.setAvatar(userUpdateDto.getAvatar());
        userEntity.setStatus(userUpdateDto.getStatus());

        userService.updateById(userEntity);

        //更新角色
        if (userUpdateDto.getRoleIds() != null) {
            roleService.assignUserRoles(userUpdateDto.getId(), userUpdateDto.getRoleIds());
        }

        log.info("更新用户成功:{}", userUpdateDto.getUsername());
        return R.ok("用户更新成功");
    }

    /**
     * @description: 通过ID删除用户信息(逻辑删除)
     * @author: JN
     * @date: 2026/1/2 17:12
     * @param: [id]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "删除用户信息")
    @OperationLog(title = "删除用户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:delete')")
    public R<String> delete(@PathVariable("id") String id) {
        // 参数校验
        if (id == null || id.trim().isEmpty()) {
            return R.error(400, "用户ID不能为空");
        }
        //  数据权限检查
        if (!userService.canEditUser(id)) {
            log.warn("用户 {} 尝试删除无权限的用户: {}",
                    securityUtils.getCurrentUsername(), id);
            return R.forbidden("无权删除该用户");
        }
        UserEntity userEntity = userService.getById(id);
        if (userEntity == null) {
            return R.error(404, "用户不存在");
        }
        // 禁止删除超级管理员
        if ("admin".equals(userEntity.getUsername())) {
            return R.error("禁止删除超级管理员");
        }
        // 获取当前用户ID的正确方式，避免比较错误
        UserEntity currentUser = securityUtils.getCurrentUser();
        if (currentUser != null && id.equals(currentUser.getId())) {
            return R.error("禁止删除自己");
        }
        //逻辑删除,1=删除,0=未删除
        userEntity.setIsDeleted(1);
        userService.removeById(userEntity);
        log.info("用户删除成功:{}", userEntity.getUsername());
        return R.ok("用户删除成功");
    }

    /**
     * @description: 根据ID 修改用户状态
     * @author: JN
     * @date: 2026/1/2 17:17
     * @param: [id, status]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "修改用户状态")
    @OperationLog(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("updateUserStatus/{id}/{status}")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:edit')")
    public R<String> updateStatus(
            @PathVariable("id") String id,
            @PathVariable("status") Integer status) {
        // ==================== 新增：数据权限检查 ====================
        if (!userService.canEditUser(id)) {
            log.warn("用户 {} 尝试修改无权限的用户状态: {}",
                    securityUtils.getCurrentUsername(), id);
            return R.forbidden("无权修改该用户状态");
        }
        Integer[] statusArray = {0, 1};
        int index = -1;
        for (int i = 0; i < statusArray.length; i++) {
            if (statusArray[i].equals(status)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return R.error("状态值不正确");
        }
        UserEntity userEntity = userService.getById(id);
        if ("admin".equals(userEntity.getUsername())) {
            return R.error("禁止修改\"admin:超级管理员\"的状态");
        }

        userEntity.setStatus(status);
        userService.updateById(userEntity);

        String statusText = status == 1 ? "启用" : "禁用";
        log.info("修改用户: {} 的状态 {}", userEntity.getUsername(), statusText);
        return R.ok("用户状态修改成功");

    }

    /**
     * @description: 通过userId重置用户密码
     * @author: JN
     * @date: 2026/1/14 12:00
     * @param: [userId]
     * @return: com.mok.baseframe.common.R<java.lang.String>
     **/
    @Operation(summary = "重置用户密码")
    @OperationLog(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd/{userId}")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:edit')")
    public R<String> resetUserPwdByUserId(@PathVariable("userId") String userId) {
        // 参数校验
        if (userId == null || userId.trim().isEmpty()) {
            return R.error(400, "用户ID不能为空");
        }
        if (!userService.canEditUser(userId)) {
            return R.error(ResponseCode.FORBIDDEN, "抱歉,您当前无权修改该用户");
        }
        // 修改：检查是否为admin用户，避免重置admin密码
        UserEntity user = userService.getById(userId);
        if (user != null && "admin".equals(user.getUsername())) {
            return R.error(403, "禁止重置超级管理员密码");
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setPassword(passwordEncoder.encode("123456"));
        Integer result = userService.updateUserPwdById(userEntity);
        return result > 0 ? R.ok("密码重置成功") : R.error("密码重置失败");
    }

    /**
     * @description: 更改用户密码
     * @author: JN
     * @date: 2026/1/15 09:44
     * @param: [userEntity]
     * @return: com.mok.baseframe.common.R<java.lang.String>
     **/
    @Operation(summary = "更改用户密码")
    @OperationLog(title = "更改用户密码", businessType = BusinessType.UPDATE)
    @PostMapping("/updatePwd")
    @PreAuthorize("@permissionChecker.hasPermission('system:user:edit')")
    public R<String> updateUserPwd(@RequestBody @Valid UserUpdateDto userUpdateDto) {
        if (!userService.canEditUser(userUpdateDto.getId())) {
            return R.error(ResponseCode.FORBIDDEN, "抱歉,您当前无权修改该用户");
        }
        if (!userUpdateDto.getPassword().equals(userUpdateDto.getConfirmPassword())) {
            return R.validationError("请检查密码两次输入是否一致");
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userUpdateDto.getId());
        userEntity.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        Integer result = userService.updateUserPwdById(userEntity);
        return result > 0 ? R.ok("密码更改成功") : R.error("密码更改失败");
    }

}
