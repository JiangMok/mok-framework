package com.mok.framework.excel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.util.Date;
import java.util.Objects;

/**
 * Excel上传测试实体类
 */
@TableName("upload_test")
public class ExcelUploadTestEntity {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 字符串内容
     */
    @ExcelProperty("字符串内容")
    private String string;
    /**
     * 日期内容
     */
    @ExcelProperty("日期内容")
    private Date date;
    /**
     * 浮点内容
     */
    @ExcelProperty("浮点内容")
    private Double doubleData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getDoubleData() {
        return doubleData;
    }

    public void setDoubleData(Double doubleData) {
        this.doubleData = doubleData;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExcelUploadTestEntity that = (ExcelUploadTestEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(string, that.string) && Objects.equals(date, that.date) && Objects.equals(doubleData, that.doubleData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, string, date, doubleData);
    }

    @Override
    public String toString() {
        return "UploadTestEntity{" +
                "id='" + id + '\'' +
                ", string='" + string + '\'' +
                ", date=" + date +
                ", doubleData=" + doubleData +
                '}';
    }
}