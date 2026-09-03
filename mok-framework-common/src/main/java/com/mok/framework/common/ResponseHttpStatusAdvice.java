package com.mok.framework.common;

import com.mok.framework.common.utils.ResponseUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 将统一响应中的标准 HTTP 状态码同步到真实 HTTP 响应。
 * 业务码（1000 及以上）仍保持 HTTP 200，由调用方读取响应体中的 code。
 */
@RestControllerAdvice
public class ResponseHttpStatusAdvice implements ResponseBodyAdvice<R<?>> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return R.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public R<?> beforeBodyWrite(R<?> body,
                                MethodParameter returnType,
                                MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request,
                                ServerHttpResponse response) {
        if (body != null) {
            response.setStatusCode(org.springframework.http.HttpStatusCode.valueOf(
                    ResponseUtils.getHttpStatus(body.getCode())));
        }
        return body;
    }
}
