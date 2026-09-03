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
            MathGenerator mathGenerator = new MathGenerator(1);
            captcha.setGenerator(mathGenerator);
        }else{
            captcha = CaptchaUtil.createShearCaptcha(
                captchaConfig.getWidth(),
                captchaConfig.getHeight(), captchaConfig.getLength(), 4);
        }
        String imageBase64 = captcha.getImageBase64Data();
        String code = captcha.getCode();
        // 2. 生成唯一标识 key（用于前端请求校验时携带）
        String key = "captcha-key_" + IdUtil.simpleUUID();

        // 3. 将验证码文本存入 Redis，并设置过期时间
        redisTemplate.opsForValue().set(key, code, captchaConfig.getExpire(), TimeUnit.SECONDS);

        // 4. 返回 key 和图片 Base64
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("image", imageBase64);
        result.put("expire", captchaConfig.getExpire());
        //记录调试日志
        //  注意：生产环境不应该记录验证码值，这里用debug级别
        log.debug("生成验证码，captcha-key: {}, code: {}", key, code);
        return result;
    }

    @Override
    public boolean validateCaptcha(String key, String code) {
        if (key == null || key.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        // 原子读取并删除，确保同一个验证码并发情况下也只能使用一次
        String expectedCode = CaptchaRedisSupport.getAndDelete(redisTemplate, key);
        if (expectedCode == null) {
            return false; // 验证码已过期或不存在
        }
        if ("math".equals(captchaConfig.getType())) {
            return new MathGenerator(1).verify(expectedCode, code.trim());
        }
        return expectedCode.equalsIgnoreCase(code.trim());
    }
}
