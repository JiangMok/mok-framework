package com.mok.framework.mail.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.framework.common.BusinessException;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.mapper.MailRecipientMapper;
import com.mok.framework.mail.mapper.MailRecipientTypeMapper;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.mail.service.MailRecipientService;
import com.mok.framework.mail.service.MailSenderService;
import com.mok.framework.mail.util.MailLogBuilder;
import com.mok.framework.model.dto.MailRecipientDTO;
import com.mok.framework.model.entity.MailLog;
import com.mok.framework.model.entity.MailRecipient;
import com.mok.framework.model.entity.MailRecipientType;
import com.mok.framework.model.enums.MailType;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收件人管理 Service 实现
 *
 * @author mok
 * @date 2026/7/17
 */
@Service
public class MailRecipientServiceImpl implements MailRecipientService {

    private static final Logger log = LogUtils.getLogger(MailRecipientServiceImpl.class);

    private final MailRecipientMapper mailRecipientMapper;
    private final MailRecipientTypeMapper mailRecipientTypeMapper;
    private final MailSenderService mailSenderService;
    private final MailLogService mailLogService;

    public MailRecipientServiceImpl(MailRecipientMapper mailRecipientMapper,
                                     MailRecipientTypeMapper mailRecipientTypeMapper,
                                     MailSenderService mailSenderService,
                                     MailLogService mailLogService) {
        this.mailRecipientMapper = mailRecipientMapper;
        this.mailRecipientTypeMapper = mailRecipientTypeMapper;
        this.mailSenderService = mailSenderService;
        this.mailLogService = mailLogService;
    }

    @Override
    public PageResult<MailRecipient> getPage(PageParam param) {
        Page<MailRecipient> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<MailRecipient> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.like(MailRecipient::getEmail, param.getKeyword())
                    .or().like(MailRecipient::getName, param.getKeyword());
        }
        wrapper.orderByDesc(MailRecipient::getCreateTime);
        IPage<MailRecipient> result = mailRecipientMapper.selectPage(page, wrapper);
        return PageResult.fromIPage(result);
    }

    @Override
    public MailRecipient getById(String id) {
        MailRecipient recipient = mailRecipientMapper.selectById(id);
        if (recipient == null) {
            throw new BusinessException("收件人不存在");
        }
        return recipient;
    }

    @Override
    @Transactional
    public void create(MailRecipientDTO dto) {
        // 保存收件人
        MailRecipient recipient = new MailRecipient();
        recipient.setId(IdUtil.simpleUUID());
        recipient.setEmail(dto.getEmail());
        recipient.setName(dto.getName());
        recipient.setStatus(dto.getStatus());
        mailRecipientMapper.insert(recipient);

        // 保存类型关联
        saveRecipientTypes(recipient.getId(), dto.getMailTypes());
    }

    @Override
    @Transactional
    public void update(MailRecipientDTO dto) {
        MailRecipient recipient = mailRecipientMapper.selectById(dto.getId());
        if (recipient == null) {
            throw new BusinessException("收件人不存在");
        }

        recipient.setEmail(dto.getEmail());
        recipient.setName(dto.getName());
        recipient.setStatus(dto.getStatus());
        mailRecipientMapper.updateById(recipient);

        // 先删除旧的类型关联，再插入新的
        mailRecipientTypeMapper.delete(new LambdaQueryWrapper<MailRecipientType>()
                .eq(MailRecipientType::getRecipientId, dto.getId()));
        saveRecipientTypes(dto.getId(), dto.getMailTypes());
    }

    @Override
    @Transactional
    public void delete(String id) {
        MailRecipient recipient = mailRecipientMapper.selectById(id);
        if (recipient == null) {
            throw new BusinessException("收件人不存在");
        }
        // 删除类型关联
        mailRecipientTypeMapper.delete(new LambdaQueryWrapper<MailRecipientType>()
                .eq(MailRecipientType::getRecipientId, id));
        // 删除收件人
        mailRecipientMapper.deleteById(id);
    }

    @Override
    public void testSend(String id) {
        MailRecipient recipient = getById(id);
        MailAccount mailAccount = mailSenderService.getMailAccount();

        String subject = "【MOK Framework】邮件测试";
        String content = "<h3>邮件发送测试</h3>" +
                "<p>您好，" + recipient.getName() + "：</p>" +
                "<p>这是一封来自 <b>MOK Framework</b> 的测试邮件，证明邮件配置正确。</p>" +
                "<p>发送时间：" + java.time.LocalDateTime.now() + "</p>";

        String messageId = IdUtil.simpleUUID();
        MailLog mailLog = MailLogBuilder.build(messageId, recipient.getEmail(), subject, content, MailType.NOTIFICATION);
        try {
            MailUtil.send(mailAccount, recipient.getEmail(), subject, content, true);
            mailLog.setSendStatus("SUCCESS");
        } catch (Exception e) {
            mailLog.setSendStatus("FAILED");
            mailLog.setFailReason(e.getMessage());
            mailLog.setRetryCount(0);
            throw new RuntimeException("测试邮件发送失败", e);
        } finally {
            mailLogService.saveOrUpdateByMessageId(mailLog);
        }
        log.info("测试邮件已发送至：{}", recipient.getEmail());
    }

    @Override
    public List<MailRecipient> listByMailType(String mailType) {
        // 先查关联表获取订阅了该类型的收件人ID
        List<MailRecipientType> types = mailRecipientTypeMapper.selectList(
                new LambdaQueryWrapper<MailRecipientType>()
                        .eq(MailRecipientType::getMailType, mailType));

        List<String> recipientIds = types.stream()
                .map(MailRecipientType::getRecipientId)
                .distinct()
                .collect(Collectors.toList());

        if (recipientIds.isEmpty()) {
            return List.of();
        }

        // 查收件人，且必须启用
        return mailRecipientMapper.selectList(new LambdaQueryWrapper<MailRecipient>()
                .in(MailRecipient::getId, recipientIds)
                .eq(MailRecipient::getStatus, 1));
    }

    /**
     * 保存收件人-类型关联
     */
    private void saveRecipientTypes(String recipientId, List<String> mailTypes) {
        if (mailTypes == null || mailTypes.isEmpty()) {
            return;
        }
        for (String mailType : mailTypes) {
            MailRecipientType type = new MailRecipientType();
            type.setId(IdUtil.simpleUUID());
            type.setRecipientId(recipientId);
            type.setMailType(mailType);
            mailRecipientTypeMapper.insert(type);
        }
    }
}
