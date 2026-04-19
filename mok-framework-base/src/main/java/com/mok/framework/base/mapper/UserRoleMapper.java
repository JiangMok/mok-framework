package com.mok.framework.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description: 用户-角色-关联mapper
 * @author: JN
 * @date: 2026/1/1 18:07
 **/
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

    /**
     * @description: 批量插入用户角色关联数据
     * @author: JN
     * @date: 2026/1/5 12:21
     * @param: [userRoleList]
     * @return: java.lang.Long
     **/
    Long insertBatch(List<UserRoleEntity> userRoleEntityList);

    /**
     * 通过 roleId 查询所有使用了某一角色的用户
     */
    List<String> selectUserIdsByRoleId(String roleId);

}