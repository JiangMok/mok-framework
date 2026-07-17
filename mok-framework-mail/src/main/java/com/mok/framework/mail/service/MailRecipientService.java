package com.mok.framework.mail.service;

import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.model.dto.MailRecipientDTO;
import com.mok.framework.model.entity.MailRecipient;

import java.util.List;

/**
 * 收件人管理 Service
 *
 * @author mok
 * @date 2026/7/17
 */
public interface MailRecipientService {

    /**
     * 分页查询收件人
     */
    PageResult<MailRecipient> getPage(PageParam param);

    /**
     * 根据ID查询
     */
    MailRecipient getById(String id);

    /**
     * 新增收件人（含类型关联）
     */
    void create(MailRecipientDTO dto);

    /**
     * 更新收件人（含类型关联）
     */
    void update(MailRecipientDTO dto);

    /**
     * 删除收件人（同时删除类型关联）
     */
    void delete(String id);

    /**
     * 测试发送 — 向指定收件人发送测试邮件
     */
    void testSend(String id);

    /**
     * 根据邮件类型查询所有启用且订阅了该类型的收件人
     */
    List<MailRecipient> listByMailType(String mailType);
}
