package com.mok.baseframe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.baseframe.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description: 角色 mapper
 * @author: JN
 * @date: 2026/1/1 18:06
 **/
@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {
    /**
     * @description: 根据 userId 获取某一用户的角色
     * @author: JN
     * @date: 2026/1/2 12:44
     * @param: [UserId]
     * @return: java.util.List<com.mok.securityframework.entity.Role>
     **/
    List<RoleEntity> selectRolesByUserId(String UserId);
}