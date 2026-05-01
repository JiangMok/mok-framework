package com.mok.framework.excel.controller;

import com.mok.framework.common.R;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.excel.entity.ExcelUploadTestEntity;
import com.mok.framework.excel.listener.ExcelUploadTestListener;
import com.mok.framework.excel.service.ExcelUploadTestService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.fesod.sheet.FesodSheet;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/excel")
public class ExcelUploadTestController {

    private static final Logger log = LogUtils.getLogger(ExcelUploadTestController.class);

    private final ExcelUploadTestService ExcelUploadTestService;

    public ExcelUploadTestController(ExcelUploadTestService ExcelUploadTestService) {
        this.ExcelUploadTestService = ExcelUploadTestService;
    }

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.error("请选择一个文件上传！");
        }

        try {
            FesodSheet.read(
                            file.getInputStream(),          // 参数1 : 文件输入流
                            ExcelUploadTestEntity.class,    // 参数2 : 映射的实体类
                            new ExcelUploadTestListener(ExcelUploadTestService)   // 参数3 : 监听器
                    )
                    .sheet()
                    .doRead();
            return R.ok("文件上传并处理成功！");
        } catch (IOException e) {
            log.error("========== 文件处理失败", e);
            return R.error("文件处理失败！");
        }
    }


    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("demo", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        FesodSheet.write(response.getOutputStream(), ExcelUploadTestEntity.class)
                .sheet("Sheet1")
                .doWrite(data());
    }

    private List<ExcelUploadTestEntity> data() {
        return ExcelUploadTestService.selectAll();
    }
}