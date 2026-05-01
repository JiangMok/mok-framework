package com.mok.framework.excel.service;

import com.mok.framework.excel.entity.ExcelUploadTestEntity;

import java.util.List;

public interface ExcelUploadTestService {

    void insertBatch(List<ExcelUploadTestEntity> excelUploadTestEntityList);

    List<ExcelUploadTestEntity> selectAll();
}
