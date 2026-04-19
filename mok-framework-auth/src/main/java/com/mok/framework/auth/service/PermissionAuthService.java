package com.mok.framework.auth.service;

import java.util.List;

public interface PermissionAuthService {

    /**
     * 通过 userId 获取用户权限列表,带缓存
     */
    List<String> listPermissionCodeByUserId(String userId);

}
