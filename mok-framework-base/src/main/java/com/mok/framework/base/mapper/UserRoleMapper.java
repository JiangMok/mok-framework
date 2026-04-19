package com.mok.baseframe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.baseframe.entity.UserRoleEntity;
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

}