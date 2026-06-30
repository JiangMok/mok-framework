package com.mok.framework.common.enums;

/**
 * 防重复提交操作类型枚举
 */
public enum PreventDuplicateType {

    /** 默认（兼容旧值"60"） */
    DEFAULT("default", "默认操作"),
    /** 提交订单 */
    SUBMIT_ORDER("order", "提交订单"),
    /** 支付 */
    PAY("pay", "支付"),
    /** 登录 */
    LOGIN("login", "登录"),
    /** 其他自定义类型 */
    OTHER("other", "其他");

    private final String code;
    private final String desc;

    PreventDuplicateType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 根据code获取枚举，找不到返回DEFAULT */
    public static PreventDuplicateType fromCode(String code) {
        if (code == null) return DEFAULT;
        for (PreventDuplicateType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return DEFAULT;
    }
}