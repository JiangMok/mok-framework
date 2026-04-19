package com.mok.framework.captcha.service;

import java.util.Map;

/**
 * @description: 图像验证码服务类
 * @author: JN
 * @date: 2026/1/1
 */
public interface CaptchaService {


    /**
     * @description: 生成验证码
     * 作用 : 生成验证码图片和相关信息
     * @author: JN
     * @date: 2026/1/1 15:27
     * @param: []
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    Map<String, Object> generateCaptcha();

    /**
     * @description: 验证验证码
     * @author: JN
     * @date: 2026/1/1 15:52
     * @param: [key, code]
     * @return: boolean
     **/
    boolean validateCaptcha(String key, String code);

}
