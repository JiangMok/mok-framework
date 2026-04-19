package com.mok.framework.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;

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


}