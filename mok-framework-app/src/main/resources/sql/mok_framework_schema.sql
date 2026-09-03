-- ============================================================================
-- MOK Framework 主业务库完整建表脚本（MySQL 8.0.13+）
--
-- 使用说明：
-- 1. 请先连接并 USE 目标主业务数据库，再执行本脚本。
-- 2. 本脚本只使用 CREATE TABLE IF NOT EXISTS，不删除、不清空、不覆盖现有数据。
-- 3. 本脚本不创建默认管理员、角色、权限，也不包含密码或其他真实凭据。
-- 4. 操作日志表属于独立存储，见：
--    mok-framework-operationLog/src/main/resources/sql/mok_operation_log.sql
-- 5. 逻辑删除表使用 MySQL 8.0.13+ 函数索引，仅约束未删除记录的业务编码唯一。
-- ============================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_dept` (
    `id`          VARCHAR(64)  NOT NULL COMMENT '部门ID',
    `dept_name`   VARCHAR(100) NOT NULL COMMENT '部门名称',
    `dept_code`   VARCHAR(64)  NOT NULL COMMENT '部门编码',
    `parent_id`   VARCHAR(64)  NOT NULL DEFAULT '0' COMMENT '父部门ID，0表示根节点',
    `ancestors`   VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '祖先ID链',
    `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '部门描述',
    `leader`      VARCHAR(100) NOT NULL DEFAULT '' COMMENT '负责人',
    `phone`       VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '联系电话',
    `email`       VARCHAR(320) NOT NULL DEFAULT '' COMMENT '邮箱',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
    `create_by`   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_dept_active_code` ((IF(`is_deleted` = 0, `dept_code`, NULL))),
    KEY `idx_sys_dept_code_deleted` (`dept_code`, `is_deleted`),
    KEY `idx_sys_dept_parent_state_sort` (`parent_id`, `is_deleted`, `status`, `sort`),
    KEY `idx_sys_dept_state` (`is_deleted`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`          VARCHAR(64)  NOT NULL COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '不可逆密码哈希',
    `nickname`    VARCHAR(100) NOT NULL COMMENT '用户昵称',
    `phone`       VARCHAR(32)  NULL COMMENT '手机号',
    `email`       VARCHAR(320) NULL COMMENT '邮箱',
    `dept_id`     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '所属部门ID',
    `avatar`      VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '头像地址',
    `create_by`   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人ID',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_active_username` ((IF(`is_deleted` = 0, `username`, NULL))),
    KEY `idx_sys_user_username_deleted` (`username`, `is_deleted`),
    KEY `idx_sys_user_dept_state` (`dept_id`, `is_deleted`, `status`),
    KEY `idx_sys_user_state` (`is_deleted`, `status`),
    KEY `idx_sys_user_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          VARCHAR(64)  NOT NULL COMMENT '角色ID',
    `role_name`   VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(100) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '角色描述',
    `create_by`   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人ID',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_active_code` ((IF(`is_deleted` = 0, `role_code`, NULL))),
    KEY `idx_sys_role_code_deleted` (`role_code`, `is_deleted`),
    KEY `idx_sys_role_state_sort` (`is_deleted`, `status`, `sort`),
    KEY `idx_sys_role_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`              VARCHAR(64)  NOT NULL COMMENT '权限ID',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(150) NOT NULL COMMENT '权限编码',
    `description`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '权限描述',
    `type`            TINYINT      NOT NULL COMMENT '类型：1菜单，2按钮，3接口',
    `parent_id`       VARCHAR(64)  NOT NULL DEFAULT '0' COMMENT '父权限ID，0表示根节点',
    `icon`            VARCHAR(100) NOT NULL DEFAULT '' COMMENT '图标',
    `path`            VARCHAR(255) NOT NULL DEFAULT '' COMMENT '前端路由',
    `component`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '前端组件路径',
    `sort`            INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `visible`         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见：0隐藏，1显示',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_permission_active_code` ((IF(`is_deleted` = 0, `permission_code`, NULL))),
    KEY `idx_sys_permission_code_deleted` (`permission_code`, `is_deleted`),
    KEY `idx_sys_permission_parent_state_sort` (`parent_id`, `is_deleted`, `status`, `sort`),
    KEY `idx_sys_permission_type_state_sort` (`type`, `is_deleted`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限与菜单表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id`          VARCHAR(64) NOT NULL COMMENT '关联ID',
    `user_id`     VARCHAR(64) NOT NULL COMMENT '用户ID',
    `role_id`     VARCHAR(64) NOT NULL COMMENT '角色ID',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
    KEY `idx_sys_user_role_role_user` (`role_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id`            VARCHAR(64) NOT NULL COMMENT '关联ID',
    `role_id`       VARCHAR(64) NOT NULL COMMENT '角色ID',
    `permission_id` VARCHAR(64) NOT NULL COMMENT '权限ID',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_permission` (`role_id`, `permission_id`),
    KEY `idx_sys_role_permission_permission_role` (`permission_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS `sys_file` (
    `id`             VARCHAR(64)   NOT NULL COMMENT '文件ID',
    `original_name`  VARCHAR(512)  NOT NULL COMMENT '原始文件名',
    `storage_name`   VARCHAR(255)  NOT NULL COMMENT '存储文件名',
    `file_path`      VARCHAR(512)  NOT NULL COMMENT '相对存储路径',
    `file_url`       VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '访问地址',
    `file_size`      BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `file_type`      VARCHAR(32)   NOT NULL DEFAULT 'other' COMMENT '文件分类',
    `mime_type`      VARCHAR(128)  NOT NULL DEFAULT '' COMMENT 'MIME类型',
    `upload_user_id` VARCHAR(64)   NOT NULL COMMENT '上传用户ID',
    `upload_ip`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '上传IP',
    `download_count` INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '下载次数',
    `business_type`  INT           NOT NULL DEFAULT 0 COMMENT '业务类型',
    `status`         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人ID',
    `update_by`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人ID',
    `is_deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_file_path` (`file_path`),
    KEY `idx_sys_file_state_time` (`is_deleted`, `status`, `create_time`),
    KEY `idx_sys_file_type_state` (`file_type`, `is_deleted`, `status`),
    KEY `idx_sys_file_uploader_time` (`upload_user_id`, `create_time`),
    KEY `idx_sys_file_business_state` (`business_type`, `is_deleted`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件元数据表';

CREATE TABLE IF NOT EXISTS `sys_mail_sender` (
    `id`           VARCHAR(64)  NOT NULL COMMENT '发件箱配置ID',
    `host`         VARCHAR(255) NOT NULL COMMENT 'SMTP服务器',
    `port`         INT          NOT NULL COMMENT 'SMTP端口',
    `ssl_enable`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否启用SSL：0否，1是',
    `from_address` VARCHAR(320) NOT NULL COMMENT '发件人地址',
    `username`     VARCHAR(255) NOT NULL COMMENT '认证用户名',
    `password`     VARCHAR(512) NOT NULL COMMENT '认证凭据，请在应用层安全保护',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sys_mail_sender_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统发件箱配置表';

CREATE TABLE IF NOT EXISTS `sys_mail_recipient` (
    `id`          VARCHAR(64)  NOT NULL COMMENT '收件人ID',
    `email`       VARCHAR(320) NOT NULL COMMENT '邮箱地址',
    `name`        VARCHAR(100) NOT NULL COMMENT '收件人名称',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_mail_recipient_email` (`email`),
    KEY `idx_sys_mail_recipient_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮件收件人表';

CREATE TABLE IF NOT EXISTS `sys_mail_recipient_type` (
    `id`           VARCHAR(64) NOT NULL COMMENT '关联ID',
    `recipient_id` VARCHAR(64) NOT NULL COMMENT '收件人ID',
    `mail_type`    VARCHAR(64) NOT NULL COMMENT '邮件类型',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_mail_recipient_type` (`recipient_id`, `mail_type`),
    KEY `idx_sys_mail_type_recipient` (`mail_type`, `recipient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收件人邮件类型关联表';

CREATE TABLE IF NOT EXISTS `mail_log` (
    `id`          VARCHAR(64)  NOT NULL COMMENT '日志ID',
    `message_id`  VARCHAR(64)  NOT NULL COMMENT '业务消息ID',
    `recipient`   VARCHAR(320) NOT NULL COMMENT '收件人地址',
    `subject`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '邮件主题',
    `mail_type`   VARCHAR(64)  NOT NULL COMMENT '邮件类型',
    `content`     MEDIUMTEXT   NULL COMMENT '邮件正文',
    `send_status` VARCHAR(32)  NOT NULL COMMENT '发送状态：SENDING/SUCCESS/FAILED',
    `fail_reason` TEXT         NULL COMMENT '失败原因',
    `send_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `retry_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '重试次数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mail_log_message_id` (`message_id`),
    KEY `idx_mail_log_status_time` (`send_status`, `send_time`),
    KEY `idx_mail_log_type_time` (`mail_type`, `send_time`),
    KEY `idx_mail_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮件发送日志表';

CREATE TABLE IF NOT EXISTS `mq_failed_message` (
    `id`                 VARCHAR(64)  NOT NULL COMMENT '失败记录ID',
    `message_id`         VARCHAR(64)  NULL COMMENT '原业务消息ID',
    `message_type`       VARCHAR(64)  NOT NULL COMMENT '消息类型',
    `message_body`       LONGTEXT     NULL COMMENT '原始消息体',
    `original_queue`     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原队列',
    `dead_queue`         VARCHAR(255) NOT NULL DEFAULT '' COMMENT '死信队列',
    `dlx_exchange`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '死信交换机',
    `dlx_routing_key`    VARCHAR(255) NOT NULL DEFAULT '' COMMENT '死信路由键',
    `fail_reason`        TEXT         NULL COMMENT '失败原因',
    `x_death_header`     TEXT         NULL COMMENT 'x-death头',
    `retry_count`        INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `max_retry`          INT UNSIGNED NOT NULL DEFAULT 2 COMMENT '最大重试次数',
    `status`             VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
    `original_timestamp` DATETIME     NULL COMMENT '原消息产生时间',
    `failed_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失败时间',
    `resolved_by`        VARCHAR(64)  NULL COMMENT '处理人ID',
    `resolved_time`      DATETIME     NULL COMMENT '处理时间',
    `remark`             VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '处理备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mq_failed_message_business` (`message_id`, `message_type`, `dead_queue`),
    KEY `idx_mq_failed_message_business` (`message_id`, `message_type`),
    KEY `idx_mq_failed_status_time` (`status`, `failed_time`),
    KEY `idx_mq_failed_type_time` (`message_type`, `failed_time`),
    KEY `idx_mq_failed_original_queue` (`original_queue`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MQ失败消息表';

CREATE TABLE IF NOT EXISTS `sys_ai_system_prompt_config` (
    `id`                       VARCHAR(64) NOT NULL COMMENT '配置ID',
    `ai_analysis_request_type` VARCHAR(64) NOT NULL COMMENT 'AI分析请求类型',
    `system_prompt`            LONGTEXT    NOT NULL COMMENT '系统提示词',
    `create_time`              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_ai_prompt_request_type` (`ai_analysis_request_type`),
    KEY `idx_sys_ai_prompt_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI系统提示词配置表';
