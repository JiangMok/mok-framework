package com.mok.framework.security;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import com.mok.framework.ai.controller.AiAnalysisController;
import com.mok.framework.ai.controller.SpringAiController;
import com.mok.framework.ai.controller.SysAiSystemPromptConfigController;
import com.mok.framework.auth.config.CorsConfig;
import com.mok.framework.auth.controller.AuthController;
import com.mok.framework.auth.service.PermissionAuthService;
import com.mok.framework.auth.service.impl.SaTokenPermissionService;
import com.mok.framework.base.controller.DepartmentController;
import com.mok.framework.base.controller.PermissionController;
import com.mok.framework.base.controller.RoleController;
import com.mok.framework.base.controller.UserController;
import com.mok.framework.base.service.RoleService;
import com.mok.framework.captcha.controller.CaptchaController;
import com.mok.framework.file.controller.FileController;
import com.mok.framework.file.controller.PublicAvatarController;
import com.mok.framework.mail.controller.MailLogController;
import com.mok.framework.mail.controller.MailRecipientController;
import com.mok.framework.mail.controller.MailSenderController;
import com.mok.framework.model.dto.RefreshTokenRequest;
import com.mok.framework.model.dto.LogoutRequest;
import com.mok.framework.model.dto.PasswordChangeRequest;
import com.mok.framework.model.dto.PasswordResetRequest;
import com.mok.framework.model.entity.RoleEntity;
import com.mok.framework.monitor.controller.MonitorController;
import com.mok.framework.mq.controller.MqFailedMessageController;
import com.mok.framework.operationLog.controller.OperationLogController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CorsFilter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityContractTest {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_GUEST = "ROLE_GUEST";

    private static final List<Class<?>> APP_CONTROLLERS = List.of(
            AuthController.class,
            CaptchaController.class,
            PublicAvatarController.class,
            MonitorController.class,
            UserController.class,
            PermissionController.class,
            DepartmentController.class,
            RoleController.class,
            FileController.class,
            AiAnalysisController.class,
            SpringAiController.class,
            SysAiSystemPromptConfigController.class,
            MailSenderController.class,
            MailRecipientController.class,
            MailLogController.class,
            MqFailedMessageController.class,
            OperationLogController.class);

    private static final Set<Class<?>> ADMIN_ONLY_CONTROLLERS = Set.of(
            DepartmentController.class,
            RoleController.class,
            FileController.class,
            AiAnalysisController.class,
            SpringAiController.class,
            SysAiSystemPromptConfigController.class,
            MailSenderController.class,
            MailRecipientController.class,
            MailLogController.class,
            MqFailedMessageController.class,
            OperationLogController.class);

    private static final Map<Class<?>, Set<String>> INTENTIONAL_PUBLIC_METHODS = Map.of(
            AuthController.class, Set.of("loadUser", "refreshToken", "logOut", "loginChallenge"),
            CaptchaController.class, Set.of("generate", "validate"),
            PublicAvatarController.class, Set.of("getAvatar"));

    @Test
    void guestIdentityWinsWithoutHardCodedBusinessPermissions() {
        PermissionAuthService permissionService = mock(PermissionAuthService.class);
        RoleService roleService = mock(RoleService.class);
        SaTokenPermissionService service = new SaTokenPermissionService(permissionService, roleService);
        List<String> configuredPermissions = List.of(
                "system:user:query", "system:user:edit", "custom:module:query");

        when(roleService.getRolesByUserId("guest-user")).thenReturn(List.of(
                role(ROLE_GUEST), role(ROLE_ADMIN)));
        when(permissionService.listPermissionCodeByUserId("guest-user"))
                .thenReturn(configuredPermissions);

        assertEquals(List.of(ROLE_GUEST), service.getRoleList("guest-user", "login"));
        assertEquals(configuredPermissions, service.getPermissionList("guest-user", "login"));
    }

    @Test
    void everySensitiveOrMutatingSurfaceRequiresAdminRole() {
        for (Class<?> controller : APP_CONTROLLERS) {
            for (Method method : controller.getDeclaredMethods()) {
                if (!isMutation(method)
                        || method.isAnnotationPresent(SaIgnore.class)
                        || isLoginLifecycleMethod(controller, method)
                        || isSelfServiceMethod(controller, method)) {
                    continue;
                }
                assertTrue(
                        hasRole(controller.getAnnotation(SaCheckRole.class), ROLE_ADMIN)
                                || hasRole(method.getAnnotation(SaCheckRole.class), ROLE_ADMIN),
                        () -> controller.getSimpleName() + "#" + method.getName()
                                + " 缺少 ROLE_ADMIN 硬门禁");
            }
        }

        for (Class<?> controller : ADMIN_ONLY_CONTROLLERS) {
            assertTrue(
                    hasRole(controller.getAnnotation(SaCheckRole.class), ROLE_ADMIN),
                    () -> controller.getSimpleName() + " 的敏感读取面缺少 ROLE_ADMIN 类级门禁");
        }
    }

    @Test
    void publicAndAuthenticationLifecycleContractsStayNarrow() {
        for (Class<?> controller : APP_CONTROLLERS) {
            Set<String> expected = INTENTIONAL_PUBLIC_METHODS.getOrDefault(controller, Set.of());
            Set<String> actual = Arrays.stream(controller.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(SaIgnore.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
            assertEquals(expected, actual, controller.getSimpleName() + " 的公开方法集合发生变化");
        }

        Method logout = findMethod(AuthController.class, "logOut");
        assertTrue(logout.isAnnotationPresent(SaIgnore.class),
                "退出接口必须允许使用 refreshToken 独立完成注销");
        assertFalse(logout.isAnnotationPresent(SaCheckLogin.class),
                "退出接口不能强制要求仍有效的 accessToken");
        assertTrue(logout.isAnnotationPresent(PostMapping.class), "退出接口必须使用 POST");
        Parameter logoutRequest = logout.getParameters()[0];
        assertEquals(LogoutRequest.class, logoutRequest.getType());
        RequestBody logoutRequestBody = logoutRequest.getAnnotation(RequestBody.class);
        assertNotNull(logoutRequestBody, "退出凭据必须使用 JSON 请求体");
        assertFalse(logoutRequestBody.required(), "仅凭有效 accessToken 也必须可以退出");

        Method refresh = findMethod(AuthController.class, "refreshToken");
        Parameter request = refresh.getParameters()[0];
        assertEquals(RefreshTokenRequest.class, request.getType());
        assertTrue(request.isAnnotationPresent(RequestBody.class), "刷新令牌必须放在 JSON 请求体中");

        Method updatePassword = findMethod(UserController.class, "updateUserPwd");
        Parameter passwordRequest = updatePassword.getParameters()[0];
        assertEquals(PasswordChangeRequest.class, passwordRequest.getType());
        assertTrue(passwordRequest.isAnnotationPresent(RequestBody.class), "修改密码必须使用专用请求体");

        Method resetPassword = findMethod(UserController.class, "resetUserPwdByUserId");
        Parameter resetRequest = resetPassword.getParameters()[1];
        assertEquals(PasswordResetRequest.class, resetRequest.getType());
        assertTrue(resetRequest.isAnnotationPresent(RequestBody.class), "重置密码必须提交新密码请求体");
    }

    @Test
    void currentUserBootstrapDoesNotRequirePermissionManagementPrivilege() {
        assertTrue(PermissionController.class.isAnnotationPresent(SaCheckLogin.class));
        for (String methodName : List.of("getMyMenus", "getApiPermissions", "getApiPermissionsByUserId")) {
            Method method = findMethod(PermissionController.class, methodName);
            assertNull(method.getAnnotation(SaCheckPermission.class),
                    methodName + " 不应要求权限管理查询权");
        }

        Method detail = findMethod(UserController.class, "detail");
        assertNull(detail.getAnnotation(SaCheckPermission.class),
                "用户详情应由数据权限判断当前用户可见范围");

        Method updatePassword = findMethod(UserController.class, "updateUserPwd");
        assertNull(updatePassword.getAnnotation(SaCheckPermission.class),
                "当前用户修改本人密码不应要求用户管理权限");
        assertNull(updatePassword.getAnnotation(SaCheckRole.class),
                "当前用户修改本人密码不应只允许管理员");
    }

    @Test
    void corsAllowsConfiguredFrontendAndRejectsUnknownOrigins() throws Exception {
        FilterRegistrationBean<CorsFilter> registration =
                new CorsConfig("http://localhost:5173,http://127.0.0.1:5173")
                        .corsFilterRegistration();
        assertEquals(Ordered.HIGHEST_PRECEDENCE, registration.getOrder());
        CorsFilter filter = registration.getFilter();
        assertNotNull(filter);

        MockHttpServletResponse allowedResponse = performPreflight(
                filter, "http://localhost:5173");
        assertEquals(200, allowedResponse.getStatus());
        assertEquals("http://localhost:5173",
                allowedResponse.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        MockHttpServletResponse allowedIpResponse = performPreflight(
                filter, "http://127.0.0.1:5173");
        assertEquals(200, allowedIpResponse.getStatus());
        assertEquals("http://127.0.0.1:5173",
                allowedIpResponse.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        MockHttpServletResponse rejectedResponse = performPreflight(
                filter, "https://untrusted.example");
        assertEquals(403, rejectedResponse.getStatus());
        assertNull(rejectedResponse.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void packagedControllerSurfaceMatchesReviewedSet() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        Set<String> expected = APP_CONTROLLERS.stream()
                .map(Class::getName)
                .collect(Collectors.toSet());
        Set<String> actual = scanner.findCandidateComponents("com.mok.framework").stream()
                .map(definition -> definition.getBeanClassName())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        assertEquals(expected, actual, "生产应用 Controller 暴露面发生变化，必须重新安全审计");
        assertFalse(actual.stream().anyMatch(name -> name.contains("TestController")));
        assertFalse(actual.stream().anyMatch(name -> name.contains("ExcelUploadTestController")));
    }

    private static boolean isMutation(Method method) {
        return method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class)
                || method.isAnnotationPresent(PatchMapping.class);
    }

    private static boolean isLoginLifecycleMethod(Class<?> controller, Method method) {
        return controller == AuthController.class
                && Set.of("loadUser", "refreshToken", "logOut", "loginChallenge").contains(method.getName());
    }

    private static boolean isSelfServiceMethod(Class<?> controller, Method method) {
        return controller == UserController.class && "updateUserPwd".equals(method.getName());
    }

    private static boolean hasRole(SaCheckRole annotation, String role) {
        return annotation != null && Arrays.asList(annotation.value()).contains(role);
    }

    private static Method findMethod(Class<?> type, String name) {
        Method method = Arrays.stream(type.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst()
                .orElse(null);
        assertNotNull(method, () -> type.getSimpleName() + "#" + name + " 不存在");
        return method;
    }

    private static RoleEntity role(String code) {
        return RoleEntity.builder().roleCode(code).build();
    }

    private static MockHttpServletResponse performPreflight(CorsFilter filter, String origin)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/system/health");
        request.addHeader(HttpHeaders.ORIGIN, origin);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
