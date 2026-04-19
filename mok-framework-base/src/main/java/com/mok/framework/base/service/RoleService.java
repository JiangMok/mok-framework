package com.mok.baseframe.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dto.RoleDTO;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.entity.RoleEntity;

import javax.management.relation.Role;
import java.util.List;

/**
 * @description: 角色service接口
 * @author: JN
 * @date: 2026/1/1
 */
public interface RoleService extends IService<RoleEntity> {

    /**
     * @description: 分页查询角色信息
     * @author: JN
     * @date: 2026/1/1 18:17
     * @param: [param]
     * @return: com.mok.securityframework.common.PageResult<com.mok.securityframework.entity.Role>
     **/
    PageResult<RoleEntity> getPageList(PageParam param);

    /**
     * @description: 获取所有可用角色
     * @author: JN
     * @date: 2026/1/1 18:18
     * @param: []
     * @return: java.util.List<com.mok.securityframework.entity.Role>
     **/
    List<RoleEntity> getAllActiveRoles();

    /**
     * @description: 通过 userId 获取角色列表
     * @author: JN
     * @date: 2026/1/1 18:19
     * @param: [userId]
     * @return: java.util.List<com.mok.securityframework.entity.Role>
     **/
    List<RoleEntity> getRolesByUserId(String userId);

    /**
     * @description: 通过 userId 给这个用户分配角色
     * @author: JN
     * @date: 2026/1/1 18:20
     * @param: [userId, roleIds]
     * @return: boolean
     **/
    boolean assignUserRoles(String userId, List<String> roleIds);

    /**
     * @description: 通过ID删除角色
     * @author: JN
     * @date: 2026/1/1 18:21
     * @param: [roleId]
     * @return: boolean
     **/
    boolean deleteRole(String roleId);

    /**
     * @description: 创建角色
     * @author: JN
     * @date: 2026/1/5 11:50
     * @param: [roleDTO]
     * @return: java.lang.Integer
     **/
    String createRole(RoleDTO roleDTO);

    /**
     * @description: 分配权限
     * @author: JN
     * @date: 2026/1/5 11:58
     * @param: [roleId, permissionIds]
     * @return: void
     **/
    void assignRolePermissions(String roleId, List<String> permissionIds);

    /**
     * @description: 修改角色
     * @author: JN
     * @date: 2026/1/5 12:18
     * @param: [roleDTO]
     * @return: boolean
     **/
    boolean updateRole(RoleDTO roleDTO);

    /**
     * @description: 根绝角色ID 查询该角色用友的 权限ID
     * @author: JN
     * @date: 2026/1/5 12:49
     * @param: [roleId]
     * @return: java.util.List<java.lang.String>
     **/
    List<String> getRolePermissionIds(String roleId);

    /**
     * @description: 根据角色 id 查询权限
     * @author: JN
     * @date: 2026/1/5 12:51
     * @param: [roleId]
     * @return: java.util.List<com.mok.securityframework.entity.Permission>
     **/
    List<PermissionEntity> getRolePermissions(String roleId);

}
