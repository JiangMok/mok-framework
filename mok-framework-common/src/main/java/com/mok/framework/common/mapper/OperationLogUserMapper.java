package com.mok.framework.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
