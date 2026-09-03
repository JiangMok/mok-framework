-- 旧库升级：为邮件投递状态机补充 message_id 唯一约束。
-- 执行前应停止应用写入并完成备份；存在重复数据时脚本会主动终止。

DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_mail_log_message_id_unique$$
CREATE PROCEDURE migrate_mail_log_message_id_unique()
BEGIN
    DECLARE duplicate_count BIGINT DEFAULT 0;
    DECLARE unique_index_count INT DEFAULT 0;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'mail_log'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'mail_log 表不存在，请先执行 mok_framework_schema.sql';
    END IF;

    SELECT COUNT(*)
    INTO duplicate_count
    FROM (
        SELECT message_id
        FROM mail_log
        WHERE message_id IS NOT NULL
        GROUP BY message_id
        HAVING COUNT(*) > 1
    ) duplicate_rows;

    IF duplicate_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'mail_log.message_id 存在重复值，请先按 README 检查并清理';
    END IF;

    SELECT COUNT(*)
    INTO unique_index_count
    FROM (
        SELECT index_name
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'mail_log'
          AND non_unique = 0
        GROUP BY index_name
        HAVING COUNT(*) = 1
           AND MAX(column_name = 'message_id') = 1
    ) unique_indexes;

    IF unique_index_count = 0 THEN
        ALTER TABLE mail_log
            ADD UNIQUE KEY uk_mail_log_message_id (message_id);
    END IF;
END$$

CALL migrate_mail_log_message_id_unique()$$
DROP PROCEDURE IF EXISTS migrate_mail_log_message_id_unique$$

DELIMITER ;
