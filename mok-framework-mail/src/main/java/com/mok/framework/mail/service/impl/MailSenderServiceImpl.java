package com.mok.framework.mail.service.impl;

import cn.hutool.core.util.IdUtil;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * 发件箱配置 Service 实现
 * 使用 AtomicReference<MailAccount> 实现热刷新，无需重启
 *
 * @author mok
 * @date 2026/7/17
 */
@Service
public class MailSenderServiceImpl implements MailSenderService {

    private final MailSenderMapper mailSenderMapper;

    /** 持有当前 MailAccount，更新配置后原子替换，实现热刷新 */
    private final AtomicReference<MailAccount> mailAccountRef = new AtomicReference<>();

    public MailSenderServiceImpl(MailSenderMapper mailSenderMapper) {
        this.mailSenderMapper = mailSenderMapper;
    }

    @Override
    public MailSender getConfig() {
        List<MailSender> list = mailSenderMapper.selectList(new LambdaQueryWrapper<MailSender>()
                .eq(MailSender::getStatus, 1));
        if (list.isEmpty()) {
            throw new BusinessException("发件箱尚未配置，请先在系统中配置系统邮箱");
        }
        return list.get(0);
    }

    @Override
    @Transactional
    public void updateConfig(MailSenderDTO dto) {
        // 先查现有配置
        List<MailSender> existing = mailSenderMapper.selectList(new LambdaQueryWrapper<MailSender>()
                .eq(MailSender::getStatus, 1));

        MailSender sender;
        if (existing.isEmpty()) {
            // 首次配置，新增
            sender = new MailSender();
            sender.setId(IdUtil.simpleUUID());
        } else {
            // 更新已存在的配置
            sender = existing.get(0);
        }

        // 仅更新密码（如果传了新密码），否则保持原密码
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            sender.setPassword(dto.getPassword());
        }

        sender.setHost(dto.getHost());
        sender.setPort(dto.getPort());
        sender.setSslEnable(dto.getSslEnable());
        sender.setFromAddress(dto.getFromAddress());
        sender.setUsername(dto.getUsername());
        sender.setStatus(dto.getStatus());

        // 保存
        if (existing.isEmpty()) {
            mailSenderMapper.insert(sender);
        } else {
            mailSenderMapper.updateById(sender);
        }

        // 热刷新 MailAccount
        refreshMailAccount(sender);
    }

    @Override
    public MailAccount getMailAccount() {
        MailAccount account = mailAccountRef.get();
        if (account == null) {
            synchronized (this) {
                account = mailAccountRef.get();
                if (account == null) {
                    // 延迟加载：从 DB 初始化
                    List<MailSender> list = mailSenderMapper.selectList(new LambdaQueryWrapper<MailSender>()
                            .eq(MailSender::getStatus, 1));
                    if (!list.isEmpty()) {
                        account = buildMailAccount(list.get(0));
                        mailAccountRef.set(account);
                    }
                }
            }
        }
        if (account == null) {
            throw new BusinessException("发件箱尚未配置，请先在系统中配置系统邮箱");
        }
        return account;
    }

    /**
     * 热刷新：根据 DB 记录重建 MailAccount 并原子替换
     */
    private void refreshMailAccount(MailSender sender) {
        MailAccount newAccount = buildMailAccount(sender);
        mailAccountRef.set(newAccount);
    }

    /**
     * 将 MailSender 实体转为 Hutool MailAccount
     */
    private MailAccount buildMailAccount(MailSender sender) {
        MailAccount account = new MailAccount();
        account.setHost(sender.getHost());
        account.setPort(sender.getPort());
        account.setSslEnable(sender.getSslEnable() == 1);
        account.setFrom(sender.getFromAddress());
        account.setUser(sender.getUsername());
        account.setPass(sender.getPassword());
        account.setAuth(true);
        return account;
    }
}
