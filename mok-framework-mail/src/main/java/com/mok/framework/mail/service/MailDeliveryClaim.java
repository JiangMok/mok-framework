package com.mok.framework.mail.service;

/**
 * 邮件投递占位结果。
 */
public enum MailDeliveryClaim {
    /** 当前调用已获得投递权。 */
    CLAIMED,
    /** 相同消息已经成功投递。 */
    ALREADY_SUCCESS,
    /** 其他实例仍持有未过期的投递权。 */
    IN_PROGRESS
}
