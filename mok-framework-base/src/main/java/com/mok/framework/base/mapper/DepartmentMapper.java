package com.mok.framework.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.DepartmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 部门 Mapper
 * @author: mok
 * @date: 2026/7/15
 **/
@Mapper
public interface DepartmentMapper extends BaseMapper<DepartmentEntity> {
}
