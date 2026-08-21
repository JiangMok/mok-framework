package com.mok.framework.operationLog.operator;

import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.base.service.DepartmentService;
import com.mok.framework.base.service.UserService;
import com.mok.framework.model.entity.DepartmentEntity;
import com.mok.framework.model.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.jiangmok.operationlog.operator.OperatorInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class FrameworkOperatorResolverTest {

    @Test
    void shouldResolveDepartmentNameByDeptId() {
        UserService userService = mock(UserService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        FrameworkOperatorResolver resolver =
                new FrameworkOperatorResolver(userService, departmentService);

        UserEntity user = new UserEntity()
                .setId("user-1")
                .setUsername("mok")
                .setNickname("测试用户")
                .setDeptId("dept-1");
        DepartmentEntity department = new DepartmentEntity()
                .setId("dept-1")
                .setDeptName("技术部");
        when(userService.getById("user-1")).thenReturn(user);
        when(departmentService.getDeptById("dept-1")).thenReturn(department);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("user-1");

            OperatorInfo result = resolver.resolve();

            assertThat(result.getOperatorId()).isEqualTo("user-1");
            assertThat(result.getOperatorName()).isEqualTo("测试用户");
            assertThat(result.getDeptName()).isEqualTo("技术部");
        }
    }
}

