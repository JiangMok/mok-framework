package com.mok.framework.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.MailRecipient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收件人 Mapper
 *
 * @author mok
 * @date 2026/7/17
 */
@Mapper
public interface MailRecipientMapper extends BaseMapper<MailRecipient> {
}
