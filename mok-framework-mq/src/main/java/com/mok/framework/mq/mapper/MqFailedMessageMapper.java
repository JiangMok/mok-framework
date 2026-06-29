package com.mok.framework.mq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.MqFailedMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MqFailedMessageMapper extends BaseMapper<MqFailedMessage> {
}
