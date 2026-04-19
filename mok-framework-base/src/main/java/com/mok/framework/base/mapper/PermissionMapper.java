package com.mok.baseframe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.baseframe.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 权限mapper
 * @author: JN
 * @date: 2026/1/1 18:08
 **/
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionEntity> {

    /**
     * @description: 根据 userId 获取权限信息
     * @author: JN
     * @date: 2026/1/2 13:29
     * @param: [userId]
     * @return: java.util.List<com.mok.securityframework.entity.Permission>
     **/
    List<PermissionEntity> selectPermissionsByUserId(String userId);

    /**
     * @description: 根据用户id和类型获取权限信息
     * @author: JN
     * @date: 2026/1/12 19:30
     * @param: [type, userId]
     * @return: java.util.List<com.mok.baseframe.entity.PermissionEntity>
     **/
    List<PermissionEntity> selectPermissionsByUserIdByType(@Param("userId")String userId,
                                                           @Param("type")Integer type);

    /**
     * @description: 根据 roleId 查询权限
     * @author: JN
     * @date: 2026/1/2 13:39
     * @param: [userId]
     * @return: java.util.List<com.mok.securityframework.entity.Permission>
     **/
    List<PermissionEntity> selectPermissionsByRoleId(String userId);

    /**
     * @description: 获取所有权限
     * @author: JN
     * @date: 2026/1/4 15:20
     * @param: []
     * @return: java.util.List<com.mok.securityframework.entity.Permission>
     **/
    List<PermissionEntity> selectAllPermissions();


}