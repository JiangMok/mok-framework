package com.mok.framework.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mok.framework.model.entity.MailLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface MailLogMapper extends BaseMapper<MailLog> {

    @Update("""
            UPDATE mail_log
            SET send_status = 'SENDING',
                fail_reason = NULL,
                retry_count = COALESCE(retry_count, 0) + 1,
                send_time = #{claimTime},
                update_time = NOW()
            WHERE message_id = #{messageId}
              AND (send_status = 'FAILED'
                   OR (send_status = 'SENDING'
                       AND update_time <= DATE_SUB(NOW(), INTERVAL #{leaseSeconds} SECOND)))
            """)
    int claimForDelivery(@Param("messageId") String messageId,
                         @Param("claimTime") LocalDateTime claimTime,
                         @Param("leaseSeconds") long leaseSeconds);

    @Update("""
            UPDATE mail_log
            SET send_status = #{sendStatus},
                fail_reason = #{failReason},
                update_time = NOW()
            WHERE message_id = #{messageId}
              AND send_status = 'SENDING'
              AND send_time = #{claimTime}
            """)
    int completeDelivery(@Param("messageId") String messageId,
                         @Param("claimTime") LocalDateTime claimTime,
                         @Param("sendStatus") String sendStatus,
                         @Param("failReason") String failReason);
}
