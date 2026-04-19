package com.mok.baseframe.base.service;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dto.PermissionDTO;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.entity.RoleEntity;

import java.util.List;
import java.util.Map;

/**
 * @description:权限service接口
 * @author: JN
 * @date: 2026/1/2
 */
public interface PermissionService {

    PageResult<PermissionEntity> getPageList(PageParam param);

    /**
     * @description: 根绝 userId 获取权限列表
     * @author: JN
     * @date: 2026/1/2 13:27
     * @param: [userId]
     * @return: java.util.List<java.lang.String>
     **/
    List<PermissionEntity> getPermissionsByUserId(String userId);

    /**
     * @description: 根据 ID 查询
     * @author: JN
     * @date: 2026/1/5 17:19
     * @param: [id]
     * @return: com.mok.securityframework.entity.Permission
     **/
    PermissionEntity getById(String id);


    /**
     * @description:根据 userId 获取菜单树
     * @author: JN
     * @date: 2026/1/2 13:27
     * @param: [userId]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    List<Map<String, Object>> getMenuTreeByUserId(String userId);

    /**
     * @description: 获取所有权限
     * @author: JN
     * @date: 2026/1/4 15:19
     * @param: []
     * @return: java.util.List<com.mok.securityframework.entity.Permission>
     **/
    List<PermissionEntity> getAllPermission();

    /**
     * @description: 获取所有未删除的权限
     * @author: JN
     * @date: 2026/1/5 14:27
     * @param: []
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    List<Map<String, Object>> getPermissionTree();

    /**
     * @description: 通过ID删除(逻辑删除)
     * @author: JN
     * @date: 2026/1/5 14:36
     * @param: [permissionId]
     * @return: boolean
     **/
    boolean deletePermission(String permissionId);

    /**
     * @description: 创建权限
     * @author: JN
     * @date: 2026/1/5 14:41
     * @param: [permissionDTO]
     * @return: java.lang.String
     **/
    String createPermission(PermissionDTO permissionDTO);

    /**
     * @description: 修改权限
     * @author: JN
     * @date: 2026/1/5 14:36
     * @param: [permissionDTO]
     * @return: boolean
     **/
    boolean updatePermission(PermissionDTO permissionDTO);

    /**
     * @description: 获取接口权限
     * @author: JN
     * @date: 2026/1/5 14:29
     * @param: []
     * @return: java.util.List<com.mok.securityframework.entity.Permission>
     **/
    List<PermissionEntity> getApiPermissions(String userId);

    /**
     * @description: 根据类型获取可用未删除的权限, 1=菜单,2=按钮,3=接口
     * @author: JN
     * @date: 2026/1/5 14:30
     * @param: [type]
     * @return: java.util.List<com.mok.securityframework.entity.Permission>
     **/
    List<PermissionEntity> selectPermissionsByUserIdByType(Integer type, String userId);

    /**
     * @description: 获取菜单树
     * @author: JN
     * @date: 2026/1/19 16:26
     * @param: []
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    List<Map<String, Object>> getMenuTree();

    /**
     * @description: 根据roleId查询权限
     * @author: JN
     * @date: 2026/1/19 16:27
     * @param: [roleId]
     * @return: java.util.List<com.mok.baseframe.entity.PermissionEntity>
     **/
    List<PermissionEntity> selectPermissionsByRoleId(String roleId);
}
