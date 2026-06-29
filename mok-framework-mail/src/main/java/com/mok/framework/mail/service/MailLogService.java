package com.mok.framework.mail.service;

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
}
