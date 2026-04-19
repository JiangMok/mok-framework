package com.mok.framework.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAuthMapper extends BaseMapper<UserEntity> {

    /**
     * @description: 修改用户密码
     * @author: JN
     * @date: 2026/1/15 09:36
     * @param: [userEntity]
     * @return: java.lang.Integer
     **/
    Integer updateUserPwdById(UserEntity userEntity);

    /**
     * 通过 username 获取用户
     *
     * @param username
     * @return
     */
    UserEntity getByUsername(String username);

}