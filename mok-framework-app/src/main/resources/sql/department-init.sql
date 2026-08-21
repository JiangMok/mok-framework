-- ================================================================
--  部门管理 DDL
--  执行前请确认当前数据库为 mf_master_dev（或对应环境库）
-- ================================================================

-- 1. 创建部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id          VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '部门ID',
    dept_name   VARCHAR(64)  NOT NULL               COMMENT '部门名称',
    dept_code   VARCHAR(64)  NOT NULL               COMMENT '部门编码',
    parent_id   VARCHAR(64)  DEFAULT '0'            COMMENT '父部门ID（0=根节点）',
    ancestors   VARCHAR(500) DEFAULT ''             COMMENT '祖先链（如 0,rootId,parentId）',
    description VARCHAR(200) DEFAULT ''             COMMENT '部门描述',
    leader      VARCHAR(32)  DEFAULT ''             COMMENT '负责人',
    phone       VARCHAR(20)  DEFAULT ''             COMMENT '联系电话',
    email       VARCHAR(64)  DEFAULT ''             COMMENT '邮箱',
    sort        INT          DEFAULT 0              COMMENT '排序',
    status      INT          DEFAULT 1              COMMENT '状态（0=停用，1=正常）',
    is_deleted  INT          DEFAULT 0              COMMENT '逻辑删除（0=正常，1=删除）',
    create_by   VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2. 用户表新增部门关联字段
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS dept_id VARCHAR(64) DEFAULT '' COMMENT '所属部门ID' AFTER email;

-- 3. 初始化一个默认根部门（可选）
-- INSERT INTO sys_dept (id, dept_name, dept_code, parent_id, ancestors, sort, status)
-- VALUES (REPLACE(UUID(), '-', ''), '总公司', 'ROOT', '0', '0', 0, 1);
