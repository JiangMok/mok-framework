package com.mok.framework.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mok.framework.auth.mapper.UserAuthMapper;
import com.mok.framework.model.entity.UserEntity;
import com.mok.framework.auth.service.UserAuthService;
import org.springframework.stereotype.Service;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserAuthMapper userMapper;

    public UserAuthServiceImpl(UserAuthMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserEntity getUserOne(UserEntity userEntity) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getUsername, userEntity.getUsername())
                .eq(UserEntity::getPassword, userEntity.getPassword());
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public UserEntity selectById(String id) {
        return userMapper.selectById(id);
    }

    @Override
    public UserEntity getByUserName(String UserName) {
        return userMapper.getByUsername(UserName);
    }
}
