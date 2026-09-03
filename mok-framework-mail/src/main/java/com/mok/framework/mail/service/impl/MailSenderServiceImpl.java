package com.mok.framework.mail.service.impl;

import cn.hutool.extra.mail.MailAccount;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mok.framework.common.BusinessException;
import com.mok.framework.mail.mapper.MailSenderMapper;
import com.mok.framework.mail.service.MailSenderService;
import com.mok.framework.model.dto.MailSenderDTO;
import com.mok.framework.model.entity.MailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 发件箱配置 Service 实现
 * 每次发送前读取唯一配置，确保禁用或改密在多实例环境中立即生效。
 *
 * @author mok
 * @date 2026/7/17
 */
@Service
public class MailSenderServiceImpl implements MailSenderService {

    private static final String SYSTEM_MAIL_SENDER_ID = "system-mail-sender";
    private static final long SMTP_TIMEOUT_MILLIS = 60_000L;

    private final MailSenderMapper mailSenderMapper;

    public MailSenderServiceImpl(MailSenderMapper mailSenderMapper) {
        this.mailSenderMapper = mailSenderMapper;
    }

    @Override
    public MailSender getConfig() {
        return copyWithoutPassword(requireUniqueConfig());
    }

    @Override
    @Transactional
    public void updateConfig(MailSenderDTO dto) {
        List<MailSender> existing = loadAllConfigs();
        ensureUnique(existing);

        MailSender sender;
        if (existing.isEmpty()) {
            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                throw new BusinessException("首次配置发件箱时必须填写认证密码");
            }
            sender = new MailSender();
            // 固定主键使并发首次配置在数据库层发生唯一键冲突，而不是创建两条记录。
            sender.setId(SYSTEM_MAIL_SENDER_ID);
        } else {
            sender = existing.get(0);
        }

        // 仅更新密码（如果传了新密码），否则保持原密码
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            sender.setPassword(dto.getPassword());
        }
        if (sender.getPassword() == null || sender.getPassword().isBlank()) {
            throw new BusinessException("发件箱认证密码不能为空");
        }

        sender.setHost(dto.getHost());
        sender.setPort(dto.getPort());
        sender.setSslEnable(dto.getSslEnable());
        sender.setFromAddress(dto.getFromAddress());
        sender.setUsername(dto.getUsername());
        sender.setStatus(dto.getStatus());

        int affectedRows;
        if (existing.isEmpty()) {
            affectedRows = mailSenderMapper.insert(sender);
        } else {
            affectedRows = mailSenderMapper.updateById(sender);
        }
        if (affectedRows != 1) {
            throw new BusinessException("发件箱配置保存失败，请重试");
        }

    }

    @Override
    public MailAccount getMailAccount() {
        MailSender sender = requireUniqueConfig();
        if (!Integer.valueOf(1).equals(sender.getStatus())) {
            throw new BusinessException("发件箱配置已禁用");
        }
        return buildMailAccount(sender);
    }

    private List<MailSender> loadAllConfigs() {
        return mailSenderMapper.selectList(new LambdaQueryWrapper<MailSender>()
                .orderByAsc(MailSender::getCreateTime));
    }

    private MailSender requireUniqueConfig() {
        List<MailSender> configs = loadAllConfigs();
        ensureUnique(configs);
        if (configs.isEmpty()) {
            throw new BusinessException("发件箱尚未配置，请先在系统中配置系统邮箱");
        }
        return configs.get(0);
    }

    private void ensureUnique(List<MailSender> configs) {
        if (configs.size() > 1) {
            throw new BusinessException("检测到多条发件箱配置，请先清理重复数据");
        }
    }

    private MailSender copyWithoutPassword(MailSender source) {
        return MailSender.builder()
                .id(source.getId())
                .host(source.getHost())
                .port(source.getPort())
                .sslEnable(source.getSslEnable())
                .fromAddress(source.getFromAddress())
                .username(source.getUsername())
                .status(source.getStatus())
                .createTime(source.getCreateTime())
                .updateTime(source.getUpdateTime())
                .build();
    }

    /**
     * 将 MailSender 实体转为 Hutool MailAccount
     */
    private MailAccount buildMailAccount(MailSender sender) {
        MailAccount account = new MailAccount();
        account.setHost(sender.getHost());
        account.setPort(sender.getPort());
        account.setSslEnable(Integer.valueOf(1).equals(sender.getSslEnable()));
        account.setFrom(sender.getFromAddress());
        account.setUser(sender.getUsername());
        account.setPass(sender.getPassword());
        account.setAuth(true);
        // 单阶段网络操作最多等待 60 秒，必须短于投递占位租约。
        account.setConnectionTimeout(SMTP_TIMEOUT_MILLIS);
        account.setTimeout(SMTP_TIMEOUT_MILLIS);
        account.setWriteTimeout(SMTP_TIMEOUT_MILLIS);
        return account;
    }
}
