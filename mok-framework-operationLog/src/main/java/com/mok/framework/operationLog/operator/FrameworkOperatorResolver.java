package com.mok.framework.operationLog.operator;

import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.base.service.DepartmentService;
import com.mok.framework.base.service.UserService;
import com.mok.framework.model.entity.DepartmentEntity;
import com.mok.framework.model.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.jiangmok.operationlog.operator.OperatorInfo;
import top.jiangmok.operationlog.operator.OperatorResolver;

/**
 * 脚手架操作人信息适配器。
 *
 * @author mok
 */
@Component
public class FrameworkOperatorResolver implements OperatorResolver {

    private final UserService userService;
    private final DepartmentService departmentService;

    public FrameworkOperatorResolver(UserService userService,
                                     DepartmentService departmentService) {
        this.userService = userService;
        this.departmentService = departmentService;
    }

    @Override
    public OperatorInfo resolve() {
        String userId = currentUserId();
        if ("SYSTEM".equals(userId)) {
            return new OperatorInfo("SYSTEM", "系统", "SYSTEM", null);
        }

        UserEntity user = userService.getById(userId);
        if (user == null) {
            return new OperatorInfo(userId, userId, "ADMIN", null);
        }

        String displayName = StringUtils.hasText(user.getNickname())
                ? user.getNickname()
                : user.getUsername();
        return new OperatorInfo(userId, displayName, "ADMIN", resolveDeptName(user));
    }

    @Override
    public String getOperatorId() {
        return currentUserId();
    }

    @Override
    public String getOperatorName() {
        return resolve().getOperatorName();
    }

    @Override
    public String getOperatorType() {
        return StpUtil.isLogin() ? "ADMIN" : "SYSTEM";
    }

    @Override
    public String getDeptName() {
        return resolve().getDeptName();
    }

    private String currentUserId() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "SYSTEM";
        } catch (Exception ignored) {
            return "SYSTEM";
        }
    }

    private String resolveDeptName(UserEntity user) {
        if (StringUtils.hasText(user.getDeptName())) {
            return user.getDeptName();
        }
        if (!StringUtils.hasText(user.getDeptId())) {
            return null;
        }
        DepartmentEntity department = departmentService.getDeptById(user.getDeptId());
        return department == null ? null : department.getDeptName();
    }
}

