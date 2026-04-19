package com.mok.framework.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.OperationLogEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @description: 操作日志 mapper
 * @author: JN
 * @date: 2026/1/5 11:24
 * @param:
 * @return:
 **/
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {

    /**
     * @description: 查询操作最多的用户
     * @author: JN
     * @date: 2026/1/5 16:48
     * @param: [limit]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @MapKey("operatorName")
    List<Map<String, Object>> selectTopOperators(Integer limit);
}