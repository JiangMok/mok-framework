package com.mok.framework.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 配置创建人查询 Mapper。
 *
 * @author mok
 */
@Mapper
public interface AiUserMapper extends BaseMapper<UserEntity> {
}

