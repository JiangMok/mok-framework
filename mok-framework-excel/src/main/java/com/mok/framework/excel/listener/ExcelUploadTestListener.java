package com.mok.framework.excel.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.excel.entity.ExcelUploadTestEntity;
import com.mok.framework.excel.service.ExcelUploadTestService;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.event.AnalysisEventListener;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel上传测试监听器
 */
public class ExcelUploadTestListener extends AnalysisEventListener<ExcelUploadTestEntity> {

    private final List<ExcelUploadTestEntity> excelUploadTestEntityList = new ArrayList<>();
    private static final Logger log = LogUtils.getLogger(ExcelUploadTestListener.class);

    private final ExcelUploadTestService excelUploadTestService;


    public ExcelUploadTestListener(ExcelUploadTestService excelUploadTestService) {
        this.excelUploadTestService = excelUploadTestService;
    }


    @Override
    public void invoke(ExcelUploadTestEntity excelUploadTestEntity, AnalysisContext analysisContext) {
        log.info("========== 读取到一条数据: {}", JSON.toJSONString(excelUploadTestEntity));
        excelUploadTestEntity.setId(IdUtil.simpleUUID());
        excelUploadTestEntityList.add(excelUploadTestEntity);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("========== 所有数据读取完毕,共: {} 条数据,开始批量保存 >>> ", excelUploadTestEntityList.size());
        excelUploadTestService.insertBatch(excelUploadTestEntityList);
        log.info("========== 数据保存完毕");
    }
}
