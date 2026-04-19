package com.mok.baseframe.entity;

import java.util.Date;
import java.util.Objects;

/**
 * @description:用户-优惠券关联实体类
 * @author: JN
 * @date: 2026/2/4
 */
public class UserCouponEntity {
    private String id;
    private String userId;
    private String couponId;
    private String couponCode;
    private Integer status;
    private String orderId;
    private Date useTime;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserCouponEntity that = (UserCouponEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(couponId, that.couponId) && Objects.equals(couponCode, that.couponCode) && Objects.equals(status, that.status) && Objects.equals(orderId, that.orderId) && Objects.equals(useTime, that.useTime) && Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime) && Objects.equals(createTime, that.createTime) && Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, couponId, couponCode, status, orderId, useTime, startTime, endTime, createTime, updateTime);
    }

    @Override
    public String toString() {
        return "UserCouponEntity{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", couponId='" + couponId + '\'' +
                ", couponCode='" + couponCode + '\'' +
                ", status=" + status +
                ", orderId='" + orderId + '\'' +
                ", useTime=" + useTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getUseTime() {
        return useTime;
    }

    public void setUseTime(Date useTime) {
        this.useTime = useTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
