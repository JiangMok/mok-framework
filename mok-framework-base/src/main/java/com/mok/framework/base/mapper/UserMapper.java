package com.mok.baseframe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.baseframe.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 用户 mapper
 * @author: JN
 * @date: 2026/1/1 18:06
 **/
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * @description: 修改用户密码
     * @author: JN
     * @date: 2026/1/15 09:36
     * @param: [userEntity]
     * @return: java.lang.Integer
     **/
    Integer updateUserPwdById(UserEntity userEntity);
}
