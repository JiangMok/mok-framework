package com.mok.framework.captcha.controller;

import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
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
    @GetMapping("/generate")
    @OperationLog(title = "获取验证码", businessType = BusinessType.INSERT)
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
    @PostMapping("/validate")
    public R<Boolean> validate(@RequestParam String key, @RequestParam String code) {
        boolean valid = captchaService.validateCaptcha(key, code);
        return valid ? R.ok(true) : R.error(400, "验证码错误");
    }
}