package com.mok.framework.model.enums;

/**
 * MQ消息类型枚举
 * 用于标识死信失败表 mq_failed_message 的 message_type 字段
 */
public enum MessageType {

    /** 操作日志 */
    OPERATION_LOG("OPERATION_LOG", "操作日志"),

    /** 系统检查 */
    SYSTEM_CHECK_MAIL("SYSTEM_CHECK_MAIL", "系统检查邮件"),

    /** 订单支付（示例扩展） */
    ORDER_PAY("ORDER_PAY", "订单支付"),

    /** 用户注册（示例扩展） */
    USER_REGISTER("USER_REGISTER", "用户注册");

    /**
     * 存入数据库的值（推荐使用英文标识，稳定且不占空间）
     */
    private final String code;

    /**
     * 中文描述，便于日志/前端展示
     */
    private final String desc;

    MessageType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举，找不到则返回 null（或可选择抛异常）
     */
    public static MessageType fromCode(String code) {
        if (code == null) return null;
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}