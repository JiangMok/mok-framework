package com.mok.framework.operationLog.controller;

import com.mok.framework.common.PageResult;
import com.mok.framework.common.R;
import com.mok.framework.operationLog.dto.OperationLogQueryRequest;
import org.junit.jupiter.api.Test;
import top.jiangmok.operationlog.entity.OperationLogEntity;
import top.jiangmok.operationlog.model.OperationLogPageResult;
import top.jiangmok.operationlog.service.OperationLogService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationLogControllerTest {

    @Test
    void shouldConvertStarterPageResultToFrameworkPageResult() {
        OperationLogService service = mock(OperationLogService.class);
        OperationLogController controller = new OperationLogController(service);
        OperationLogQueryRequest request = new OperationLogQueryRequest();
        request.setPageNum(2);
        request.setPageSize(10);
        request.setKeyword("用户");
        request.setStatus(0);

        OperationLogEntity entity = new OperationLogEntity()
                .setId("log-1")
                .setTitle("新增用户")
                .setStatus(0);
        when(service.pageQueryResult(eq(2), eq(10), eq("用户"), anyMap()))
                .thenReturn(new OperationLogPageResult(List.of(entity), 21, 2, 10));

        R<PageResult<OperationLogEntity>> response = controller.page(request);

        assertThat(response.getData().getTotal()).isEqualTo(21);
        assertThat(response.getData().getPageNum()).isEqualTo(2);
        assertThat(response.getData().getData()).containsExactly(entity);
    }

    @Test
    void shouldReturnOperationLogDetail() {
        OperationLogService service = mock(OperationLogService.class);
        OperationLogController controller = new OperationLogController(service);
        OperationLogEntity entity = new OperationLogEntity().setId("log-1");
        when(service.findById("log-1")).thenReturn(entity);

        assertThat(controller.detail("log-1").getData()).isSameAs(entity);
    }
}

