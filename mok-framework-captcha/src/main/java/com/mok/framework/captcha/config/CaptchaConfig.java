package com.mok.baseframe.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "captcha")
public class CaptchaConfig  {
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
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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