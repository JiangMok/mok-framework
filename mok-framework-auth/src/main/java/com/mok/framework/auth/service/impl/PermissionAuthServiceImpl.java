package com.mok.framework.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mok.framework.auth.mapper.PermissionAuthMapper;
import com.mok.framework.auth.mapper.UserAuthMapper;
import com.mok.framework.auth.service.PermissionAuthService;
import com.mok.framework.common.constant.PermissionCacheConstant;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.entity.UserEntity;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PermissionAuthServiceImpl implements PermissionAuthService {

    private static final Logger log = LogUtils.getLogger(PermissionAuthServiceImpl.class);

    private final PermissionAuthMapper permissionMapper;
    private final UserAuthMapper userAuthMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public PermissionAuthServiceImpl(PermissionAuthMapper permissionMapper,
                                    RedisTemplate<String, Object> redisTemplate,
                                    UserAuthMapper userAuthMapper) {
        this.permissionMapper = permissionMapper;
        this.redisTemplate = redisTemplate;
        this.userAuthMapper = userAuthMapper;
    }

    @Override
    public List<String> listPermissionCodeByUserId(String userId) {
        // 1. 检查用户是否存在,不存在直接返回空列表
        if (!checkUserExists(userId)) {
            return Collections.emptyList();
        }
        // 2. 用户存在,先检查 redis 缓存
        String permissionKey = String.format(PermissionCacheConstant.USER_PERMISSION_KEY, userId);
        Object cached = redisTemplate.opsForValue().get(permissionKey);
        // 2.1 缓存命中 : 空值标记
        if (PermissionCacheConstant.NULL_VALUE.equals(cached)) {
            log.debug("========== 缓存命中空值标记，用户 {} 无权限", userId);
            return Collections.emptyList();
        }
        // 2.2 缓存命中 : 正常返回
        if (cached instanceof List) {
            List<String> permissionList = (List<String>) cached;
            log.debug("========== 缓存命中权限列表，用户 {} 拥有 {} 个权限", userId, permissionList.size());
            return permissionList;
        }
        // 2.3 缓存未命中 : 查询数据库
        log.debug("========== 缓存未命中权限列表，用户 {} 开始查询数据库权限",userId);
        List<String> permissionCodeList = permissionMapper.selectPermissionCodeByUserId(userId);
        if (permissionCodeList == null) {
            cacheNullValue(permissionKey);
            return Collections.emptyList();
        }
        // 2.4 数据库中存在,则先缓存数据,然后返回数据
        redisTemplate.opsForValue().set(permissionKey, permissionCodeList, PermissionCacheConstant.CACHE_EXPIRE, TimeUnit.MINUTES);
        log.info("========== 用户{}权限已缓存，数量：{}", userId, permissionCodeList.size());
        return permissionCodeList;
    }

    /**
     * 检查用户是否存在
     */
    private Boolean checkUserExists(String userId) {
        // 1.创建用户存在key
        String userExistedKey = String.format(PermissionCacheConstant.USER_EXISTS_KEY, userId);
        // 2.向redis查询用户是否在缓存中
        Object cached = redisTemplate.opsForValue().get(userExistedKey);
        // 3.如果对象对象不为空,说明存在,返回true
        if (cached != null) {
            return Boolean.valueOf(cached.toString());
        }
        // 4.如果不存在,查询一下数据库
        LambdaQueryWrapper<UserEntity> userEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userEntityLambdaQueryWrapper.eq(UserEntity::getId, userId);
        boolean userExisted = userAuthMapper.exists(userEntityLambdaQueryWrapper);
        // 5.综合判断是否存在数据库中,然后缓存存在的数据或者不存在的数据
        log.info("========== 用户>>{}>>{}>>于系统数据库", userId, userExisted ? "存在" : "不存在");
        // 5.1创建缓存时长:存在>>缓存30分钟,不存在>>>缓存5分钟
        long expireTime = userExisted ?
                PermissionCacheConstant.CACHE_EXPIRE : PermissionCacheConstant.NULL_CACHE_EXPIRE;
        // 5.2开始缓存
        redisTemplate.opsForValue().set(
                // 参数1 : 用户存在key
                userExistedKey,
                // 参数2: key 对应的 value
                String.valueOf(userExisted),
                // 参数3 : 过期时间
                expireTime,
                // 参数4 : 过期时间单位
                TimeUnit.MINUTES);
        // 6.返回结果
        return userExisted;
    }

    /**
     * 缓存空值
     */
    private void cacheNullValue(String key) {
        redisTemplate.opsForValue().set(
                key,
                PermissionCacheConstant.NULL_VALUE,
                PermissionCacheConstant.NULL_CACHE_EXPIRE,
                TimeUnit.MINUTES
        );
    }

}
