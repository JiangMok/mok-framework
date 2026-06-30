package com.mok.framework.captcha.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.util.IdUtil;
import com.mok.framework.captcha.config.CaptchaConfig;
import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码实现类（基于 Hutool + Redis）
 */
@Service
@ConditionalOnProperty(name = "captchaImpl.generate.type", havingValue = "hutool")
public class CaptchaHutoolServiceImpl implements CaptchaService {

    private final static Logger log = LogUtils.getLogger(CaptchaHutoolServiceImpl.class);

    // 验证码在 Redis 中的缓存时间（单位：秒）
    private static final long CAPTCHA_EXPIRE_SECONDS = 300; // 5分钟

    private final StringRedisTemplate redisTemplate;
    private final CaptchaConfig captchaConfig;

    public CaptchaHutoolServiceImpl(StringRedisTemplate redisTemplate,
                                    CaptchaConfig captchaConfig) {
        this.redisTemplate = redisTemplate;
        this.captchaConfig = captchaConfig;
    }

    @Override
    public Map<String, Object> generateCaptcha() {
        ShearCaptcha captcha;
        if("math".equals(captchaConfig.getType())){
            captcha = CaptchaUtil.createShearCaptcha(
                    captchaConfig.getWidth(),
                    captchaConfig.getHeight());
            // 获取验证码文本（全小写，便于校验忽略大小写）
            captcha.setGenerator(new MathGenerator(1));
        }else{
            captcha = CaptchaUtil.createShearCaptcha(
                captchaConfig.getWidth(),
                captchaConfig.getHeight(), 4, 4);
        }
        String imageBase64 = captcha.getImageBase64Data();
        String code = captcha.getCode();
        // 2. 生成唯一标识 key（用于前端请求校验时携带）
        String key = "captcha-key_" + IdUtil.simpleUUID();

        // 3. 将验证码文本存入 Redis，并设置过期时间
        redisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 4. 返回 key 和图片 Base64
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("image", imageBase64);
        //记录调试日志
        //  注意：生产环境不应该记录验证码值，这里用debug级别
        log.debug("生成验证码，captcha-key: {}, code: {}", key, code);
        return result;
    }

    @Override
    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        // 1. 从 Redis 中获取正确的验证码
        String expectedCode = redisTemplate.opsForValue().get(key);
        if (expectedCode == null) {
            return false; // 验证码已过期或不存在
        }
        // 2. 校验（忽略大小写，去除前后空格）
        boolean isValid = expectedCode.equalsIgnoreCase(code.trim());
        // 3. 校验通过后，立即删除验证码（防止重复使用）
        if (isValid) {
            redisTemplate.delete(key);
        }
        return isValid;
    }
}