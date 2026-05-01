package com.mok.framework.excel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mok.framework.excel.entity.ExcelUploadTestEntity;
import com.mok.framework.excel.mapper.ExcelUploadTestMapper;
import com.mok.framework.excel.service.ExcelUploadTestService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExcelUploadTestServiceImpl implements ExcelUploadTestService {

    private final ExcelUploadTestMapper excelUploadTestMapper;

    public ExcelUploadTestServiceImpl(ExcelUploadTestMapper excelUploadTestMapper) {
        this.excelUploadTestMapper = excelUploadTestMapper;
    }

    @Override
    public void insertBatch(List<ExcelUploadTestEntity> excelUploadTestEntityList) {
        excelUploadTestMapper.insertBatch(excelUploadTestEntityList);
    }

    @Override
    public List<ExcelUploadTestEntity> selectAll() {
        LambdaQueryWrapper<ExcelUploadTestEntity> wrapper = new LambdaQueryWrapper<>();
        return excelUploadTestMapper.selectList(wrapper);
    }
}
