package com.mok.framework.auth.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.mok.framework.auth.service.PermissionAuthService;
import com.mok.framework.base.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 * Satoken 权限注解管理实现类
 */
@Service
public class SaTokenPermissionService implements StpInterface {

    private final PermissionAuthService permissionAuthService;
    private final RoleService roleService;

    public SaTokenPermissionService(PermissionAuthService permissionAuthService,
                                    RoleService roleService) {
        this.permissionAuthService = permissionAuthService;
        this.roleService = roleService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (loginId == null) {
            return List.of();
        }
        return permissionAuthService.listPermissionCodeByUserId(loginId.toString());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return List.of();
        }
        List<String> roles = roleService.getRolesByUserId(loginId.toString()).stream()
                .map(role -> role.getRoleCode())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return roles.contains("ROLE_GUEST") ? List.of("ROLE_GUEST") : roles;
    }


}
