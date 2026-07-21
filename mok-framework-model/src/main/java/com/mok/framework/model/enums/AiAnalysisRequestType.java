package com.mok.framework.model.enums;

public enum AiAnalysisRequestType {

    OPERATION_LOG("OPERATION_LOG","操作日志类型"),
    MQ_FAILED_MESSAGE("MQ_FAILED_MESSAGE","MQ失败消息类型");

    private final String code;
    private final String desc;


    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    AiAnalysisRequestType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AiAnalysisRequestType fromCode(String code) {
        for (AiAnalysisRequestType type : values()) {
            if (type.code.equals(code)) return type;
        }
        return null;
    }
}
