package com.mok.framework.auth.service;

import com.mok.framework.model.entity.UserEntity;

public interface UserService {

    /**
     * 获取一个用户
     *
     * @param userEntity
     * @return
     */
    UserEntity getUserOne(UserEntity userEntity);

    /**
     * 通过 id 获取用户
     *
     * @param id
     * @return
     */
    UserEntity selectById(String id);

    /**
     * 通过 userName 获取用户
     *
     * @param UserName
     * @return
     */
    UserEntity getByUserName(String UserName);
}
