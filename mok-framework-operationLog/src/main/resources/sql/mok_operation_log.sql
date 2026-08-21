-- 操作日志 Starter 1.1 MySQL 表
CREATE TABLE IF NOT EXISTS `mok_operation_log` (
    `id`             VARCHAR(64)  NOT NULL COMMENT '日志主键',
    `title`          VARCHAR(100) DEFAULT '' COMMENT '操作标题',
    `business_type`  VARCHAR(50)  DEFAULT '' COMMENT '业务类型',
    `method`         VARCHAR(255) DEFAULT '' COMMENT '方法名',
    `request_method` VARCHAR(20)  DEFAULT '' COMMENT '请求方式',
    `operator_type`  VARCHAR(50)  DEFAULT '' COMMENT '操作人类型',
    `operator_id`    VARCHAR(64)  DEFAULT '' COMMENT '操作人ID',
    `operator_name`  VARCHAR(100) DEFAULT '' COMMENT '操作人名称快照',
    `dept_name`      VARCHAR(100) DEFAULT '' COMMENT '部门名称快照',
    `oper_url`       VARCHAR(500) DEFAULT '' COMMENT '请求URL',
    `oper_ip`        VARCHAR(50)  DEFAULT '' COMMENT '操作IP',
    `oper_location`  VARCHAR(100) DEFAULT '' COMMENT '操作地点',
    `oper_param`     TEXT         COMMENT '请求参数',
    `json_result`    TEXT         COMMENT '响应结果',
    `status`         TINYINT      DEFAULT 0 COMMENT '0成功 1系统失败 2业务失败',
    `error_msg`      TEXT         COMMENT '错误信息',
    `oper_time`      DATETIME     COMMENT '操作时间',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `retry_count`    INT          DEFAULT 0 COMMENT 'MQ重试次数',
    PRIMARY KEY (`id`),
    INDEX `idx_oper_time` (`oper_time`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_operator_name` (`operator_name`),
    INDEX `idx_status` (`status`),
    INDEX `idx_business_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 如需迁移旧 sys_operation_log 数据，请在确认字段后执行一次 INSERT ... SELECT，
-- 旧表不会由脚手架自动删除。

