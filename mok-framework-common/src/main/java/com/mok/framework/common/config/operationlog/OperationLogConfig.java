package com.mok.framework.common.config.operationlog;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "operation-log")
public class OperationLogConfig  {
    private Boolean enabled = true;
    private Boolean recordGet = true;
    private Integer maxContentLength = 2000;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getRecordGet() {
        return recordGet;
    }

    public void setRecordGet(Boolean recordGet) {
        this.recordGet = recordGet;
    }

    public Integer getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(Integer maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    @Override
    public String toString() {
        return "OperationLogConfig{" +
                "enabled=" + enabled +
                ", recordGet=" + recordGet +
                ", maxContentLength=" + maxContentLength +
                '}';
    }
}