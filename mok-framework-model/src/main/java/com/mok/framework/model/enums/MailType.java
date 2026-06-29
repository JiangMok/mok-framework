package com.mok.framework.model.enums;

/**
 * 邮件类型枚举
 */
public enum MailType {

    SYSTEM_CHECK("SYSTEM_CHECK", "系统检查邮件"),
    ALERT("ALERT", "告警邮件"),
    NOTIFICATION("NOTIFICATION", "通知邮件");

    private final String code;
    private final String desc;

    MailType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static MailType fromCode(String code) {
        for (MailType type : values()) {
            if (type.code.equals(code)) return type;
        }
        return null;
    }
}