-- =====================================================================
-- 快马日结 - 余额查询模块数据库脚本
-- 适用于 MySQL 8.0+
-- =====================================================================

-- =====================================================================
-- 表: boss_merchant_account (老板商户账户)
-- 说明: 老板的商户号账户信息与可用余额，金额以「分」存储
-- =====================================================================
CREATE TABLE IF NOT EXISTS `boss_merchant_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `date` DATE COMMENT '创建日期',
    `create_by` BIGINT COMMENT '创建人ID',
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `boss_id` BIGINT NOT NULL COMMENT '所属老板 user.id',
    `account_name` VARCHAR(128) DEFAULT NULL COMMENT '账户名称，如 晴时科技',
    `subject_name` VARCHAR(255) DEFAULT NULL COMMENT '主体全称，如 上海晴时网络科技有限公司',
    `merchant_no` VARCHAR(64) DEFAULT NULL COMMENT '商户号',
    `balance` BIGINT DEFAULT 0 COMMENT '账户余额（分）',
    `is_default` TINYINT(1) DEFAULT 1 COMMENT '是否默认账户',
    `status` VARCHAR(16) DEFAULT 'active' COMMENT '状态:active正常/frozen冻结',
    PRIMARY KEY (`id`),
    KEY `idx_bma_boss` (`boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老板商户账户（余额查询）';

-- =====================================================================
-- 示例数据: boss_merchant_account
-- =====================================================================
INSERT INTO `boss_merchant_account` (`boss_id`, `account_name`, `subject_name`, `merchant_no`, `balance`, `is_default`, `status`)
VALUES
(1001, '晴时科技', '上海晴时网络科技有限公司', '95017123886', 12791800, 1, 'active');
