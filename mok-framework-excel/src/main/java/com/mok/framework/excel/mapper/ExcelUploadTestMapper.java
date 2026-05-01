package com.mok.framework.excel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.excel.entity.ExcelUploadTestEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExcelUploadTestMapper extends BaseMapper<ExcelUploadTestEntity> {

    /**
     * 批量插入
     * @return
     */
    Long insertBatch(List<ExcelUploadTestEntity> excelUploadTestEntityList);

}
