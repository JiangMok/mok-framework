package com.mok.framework.captcha.service.impl;

import com.mok.framework.captcha.config.CaptchaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class CaptchaServiceImplTest {

    @Test
    void hutoolMathCaptchaValidatesTheCalculatedAnswer() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("captcha-key")),
                any(Object[].class)))
                .thenReturn("1+2=");
        CaptchaConfig config = config("math", 300);
        CaptchaHutoolServiceImpl service = new CaptchaHutoolServiceImpl(redisTemplate, config);

        assertThat(service.validateCaptcha("captcha-key", "3")).isTrue();
        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of("captcha-key")),
                any(Object[].class));
    }

    @Test
    void mokCaptchaIsAtomicallyConsumedEvenAfterAnInvalidAttempt() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("captcha-key")),
                any(Object[].class)))
                .thenReturn("AbCd")
                .thenReturn(null);
        CaptchaMOKServiceImpl service = new CaptchaMOKServiceImpl(
                redisTemplate, config("char", 300));

        assertThat(service.validateCaptcha("captcha-key", "wrong")).isFalse();
        assertThat(service.validateCaptcha("captcha-key", "abcd")).isFalse();
        verify(redisTemplate, org.mockito.Mockito.times(2))
                .execute(any(RedisScript.class), eq(List.of("captcha-key")),
                        any(Object[].class));
    }

    @Test
    void bothImplementationsUseConfiguredExpiryAndReturnTheSameFields() {
        CaptchaConfig config = config("char", 45);

        RedisTemplate<String, String> mokRedisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> mokOperations = mock(ValueOperations.class);
        when(mokRedisTemplate.opsForValue()).thenReturn(mokOperations);
        Map<String, Object> mokResult = new CaptchaMOKServiceImpl(mokRedisTemplate, config)
                .generateCaptcha();

        StringRedisTemplate hutoolRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> hutoolOperations = mock(ValueOperations.class);
        when(hutoolRedisTemplate.opsForValue()).thenReturn(hutoolOperations);
        Map<String, Object> hutoolResult = new CaptchaHutoolServiceImpl(hutoolRedisTemplate, config)
                .generateCaptcha();

        assertThat(mokResult.keySet()).containsExactlyInAnyOrder("key", "image", "expire");
        assertThat(hutoolResult.keySet()).containsExactlyInAnyOrder("key", "image", "expire");
        assertThat(mokResult.get("expire")).isEqualTo(45);
        assertThat(hutoolResult.get("expire")).isEqualTo(45);
        assertThat(mokResult.get("image").toString()).startsWith("data:image/");
        assertThat(hutoolResult.get("image").toString()).startsWith("data:image/");
        verify(mokOperations).set(anyString(), anyString(), eq(45L), eq(TimeUnit.SECONDS));
        verify(hutoolOperations).set(anyString(), anyString(), eq(45L), eq(TimeUnit.SECONDS));
    }

    private CaptchaConfig config(String type, int expire) {
        CaptchaConfig config = new CaptchaConfig();
        config.setType(type);
        config.setExpire(expire);
        return config;
    }
}
