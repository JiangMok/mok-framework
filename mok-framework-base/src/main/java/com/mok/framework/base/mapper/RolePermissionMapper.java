package com.mok.baseframe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.baseframe.entity.RolePermissionEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description: 角色-权限-关联mapper
 * @author: JN
 * @date: 2026/1/1 18:08
 **/
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionEntity> {

    /**
     * @description: 批量插入角色权限
     * @author: JN
     * @date: 2026/1/5 12:11
     * @param: []
     * @return: java.lang.Long
     **/
    Long insertBatch(List<RolePermissionEntity> permissionList);

}