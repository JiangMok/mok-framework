package com.mok.framework.captcha.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mok.captcha")
public class CaptchaConfig {
    private Integer width = 120;
    private Integer height = 40;
    private Integer length = 4;
    private Integer expire = 300;
    private String type = "math";

    // Getter/Setter
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        requirePositive(width, "验证码宽度");
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        requirePositive(height, "验证码高度");
        this.height = height;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        requirePositive(length, "验证码长度");
        this.length = length;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        requirePositive(expire, "验证码有效期");
        this.expire = expire;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (!"math".equals(type) && !"char".equals(type)) {
            throw new IllegalArgumentException("验证码类型仅支持 math 或 char");
        }
        this.type = type;
    }

    private void requirePositive(Integer value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + "必须大于0");
        }
    }

    @Override
    public String toString() {
        return "CaptchaConfig{" +
                "width=" + width +
                ", height=" + height +
                ", length=" + length +
                ", expire=" + expire +
                ", type='" + type + '\'' +
                '}';
    }
}
