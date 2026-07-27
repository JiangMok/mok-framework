package com.mok.framework.captcha.controller;

import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.ratelimiter.annotation.PreventDuplicate;
import com.mok.framework.ratelimiter.annotation.RateLimit;
import com.mok.framework.ratelimiter.enums.RateLimitScope;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 获取验证码
     *
     * @return
     */
    @OperationLog(title = "获取验证码", businessType = BusinessType.INSERT)
    @RateLimit(scope = RateLimitScope.IP, limit = 10, message = "验证码获取过于频繁，请稍后重试")
    @PreventDuplicate(lockTime = 3, message = "请勿重复获取验证码")
    @GetMapping("/generate")
    public R<Map<String, Object>> generate() {
        return R.ok(captchaService.generateCaptcha());
    }

    /**
     * 验证 验证码
     *
     * @param key
     * @param code
     * @return
     */
    @RateLimit(scope = RateLimitScope.IP, limit = 20, message = "验证码验证过于频繁，请稍后重试")
    @PostMapping("/validate")
    public R<Boolean> validate(@RequestParam String key, @RequestParam String code) {
        boolean valid = captchaService.validateCaptcha(key, code);
        return valid ? R.ok(true) : R.error(400, "验证码错误");
    }
}