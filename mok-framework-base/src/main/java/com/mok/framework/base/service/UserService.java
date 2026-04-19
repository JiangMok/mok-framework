package com.mok.baseframe.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.entity.UserEntity;

import java.util.List;

/**
 * @description: User service 接口
 * @author: JN
 * @date: 2025/12/31
 */
public interface UserService extends IService<UserEntity> {

    /**
     * @description: 分页查询
     * @author: JN
     * @date: 2026/1/6 10:40
     * @param: [param]
     * @return: com.mok.securityframework.common.PageResult<com.mok.securityframework.entity.User>
     **/
    PageResult<UserEntity> getPageList(PageParam param);

    /**
     * @description: 根据 userId 查询权限
     * @author: JN
     * @date: 2026/1/6 10:40
     * @param: [userId]
     * @return: java.util.List<java.lang.String>
     **/
    List<String> getPermissionsByUserId(String userId);

    // ==================== 新增：数据权限相关方法 ====================

    /**
     * @description: 获取用户分页列表（带数据权限控制）
     * @author: JN
     * @date: 2026/1/7
     * @param: [param, applyDataPermission 是否应用数据权限]
     * @return: com.mok.securityframework.common.PageResult<com.mok.securityframework.entity.User>
     **/
    PageResult<UserEntity> getPageListWithPermission(PageParam param, boolean applyDataPermission);

    /**
     * @description: 检查当前用户是否有权限查看指定用户
     * @author: JN
     * @date: 2026/1/7
     * @param: [targetUserId 目标用户ID]
     * @return: boolean true-有权限，false-无权限
     **/
    boolean canViewUser(String targetUserId);

    /**
     * @description: 检查当前用户是否有权限修改指定用户
     * @author: JN
     * @date: 2026/1/7
     * @param: [targetUserId 目标用户ID]
     * @return: boolean true-有权限，false-无权限
     **/
    boolean canEditUser(String targetUserId);

    /**
     * @description: 通过用户ID更新密码
     * @author: JN
     * @date: 2026/1/14 11:50
     * @param: [serId]
     * @return: boolean
     **/
    Integer updateUserPwdById(UserEntity userEntity);
}
