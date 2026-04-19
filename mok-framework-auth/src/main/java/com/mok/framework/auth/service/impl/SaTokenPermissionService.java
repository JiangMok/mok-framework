package com.mok.framework.auth.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.auth.service.PermissionAuthService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Satoken 权限注解管理实现类
 */
@Service
public class SaTokenPermissionService implements StpInterface {

    private final PermissionAuthService permissionAuthService;

    public SaTokenPermissionService(PermissionAuthService permissionAuthService) {
        this.permissionAuthService = permissionAuthService;
    }

    @Override
    public List<String> getPermissionList(Object o, String s) {
        return permissionAuthService.listPermissionCodeByUserId(StpUtil.getLoginIdAsString());
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        return List.of();
    }


}
