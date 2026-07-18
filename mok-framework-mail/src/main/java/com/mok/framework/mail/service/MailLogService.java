package com.mok.framework.mail.service;

import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.model.entity.MailLog;

public interface MailLogService {

    /**
     * @description:  保存邮件记录
     * @author: mok
     * @date: 2026/6/29 14:49
     * @param: [mailLog]
     * @return: void
    **/
    void saveMailLog(MailLog mailLog);

    /**
     * @description: 通过messageId的获取邮件记录
     * @author: mok
     * @date: 2026/6/29 15:49
     * @param: [id]
     * @return: com.mok.framework.model.entity.MailLog
    **/
    MailLog getMailLogByMessageId(String messageId);

    /**
     * @description:  通过ID更新
     * @author: mok
     * @date: 2026/6/29 16:07
     * @param: [id]
     * @return: void
    **/
    void updateById(MailLog mailLog);

    /**
     * @description:  保存或更新邮件日志（按 messageId 判断）
     * @author: mok
     * @date: 2026/6/30 13:52
     * @param: [mailLog]
     * @return: void
    **/
    void saveOrUpdateByMessageId(MailLog mailLog);

    /**
     * @description: 分页查询邮件日志
     * @author: mok
     * @date: 2026/7/18
     * @param: [param]
     * @return: com.mok.framework.common.PageResult<com.mok.framework.model.entity.MailLog>
    **/
    PageResult<MailLog> getPage(PageParam param);

    /**
     * @description: 根据ID查询邮件日志
     * @author: mok
     * @date: 2026/7/18
     * @param: [id]
     * @return: com.mok.framework.model.entity.MailLog
    **/
    MailLog getById(String id);

    /**
     * @description: 根据ID删除邮件日志
     * @author: mok
     * @date: 2026/7/18
     * @param: [id]
     * @return: void
    **/
    void deleteById(String id);
}
