package com.mok.framework.common.enums;

/**
 * 业务操作类型
 */
public enum BusinessType {

    LOGIN("登录"),
    LOGOUT("登出"),
    OTHER("其它"),
    QUERY("查询"),
    INSERT("新增"),
    UPDATE("修改"),
    DELETE("删除"),
    GRANT("授权"),
    EXPORT("导出"),
    IMPORT("导入"),
    FORCE("强退"),
    CLEAN("清空数据");

    private final String value;

    BusinessType(String value) {
        this.value = value;
    }

    /**
     * 获取业务操作类型描述
     * @return 操作类型描述
     */
    public String getValue() {
        return value;
    }

    /**
     * 根据value获取对应的枚举
     * @param value 业务操作类型描述
     * @return 对应的枚举，如果未找到则返回null
     */
    public static BusinessType getByValue(String value) {
        for (BusinessType type : BusinessType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据name获取对应的枚举
     * @param name 枚举名称
     * @return 对应的枚举，如果未找到则返回null
     */
    public static BusinessType getByName(String name) {
        try {
            return BusinessType.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}