package com.mok.framework.model.validation;

/**
 * 系统统一密码策略。
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 20;
    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,20}$";
    public static final String MESSAGE = "密码需包含大写字母、小写字母和数字，长度8-20位且不能包含空白字符";

    private PasswordPolicy() {
    }
}
