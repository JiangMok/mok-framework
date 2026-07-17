package com.mok.framework.mail.service;

import cn.hutool.extra.mail.MailAccount;
import com.mok.framework.model.dto.MailSenderDTO;
import com.mok.framework.model.entity.MailSender;

/**
 * 发件箱配置 Service
 *
 * @author mok
 * @date 2026/7/17
 */
public interface MailSenderService {

    /**
     * 获取发件箱配置（有且仅有一条）
     */
    MailSender getConfig();

    /**
     * 更新发件箱配置，同时热刷新 MailAccount
     */
    void updateConfig(MailSenderDTO dto);

    /**
     * 获取当前可用的 MailAccount（用于发送邮件）
     */
    MailAccount getMailAccount();
}
