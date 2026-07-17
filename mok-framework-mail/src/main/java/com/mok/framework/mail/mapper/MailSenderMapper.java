package com.mok.framework.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.MailSender;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发件箱配置 Mapper
 *
 * @author mok
 * @date 2026/7/17
 */
@Mapper
public interface MailSenderMapper extends BaseMapper<MailSender> {
}
