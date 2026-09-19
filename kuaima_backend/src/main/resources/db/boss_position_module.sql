-- =============================================================
-- 岗位管理模块（老板端 uniapp）
-- 适用：MySQL 8.x / InnoDB / utf8mb4
-- 说明：表结构可由 JPA ddl-auto=update 自动建表；本脚本用于
--       初始化、CI 环境与手动补表，可重复执行（IF NOT EXISTS）。
-- =============================================================

CREATE TABLE IF NOT EXISTS `boss_position` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`              VARCHAR(100) NOT NULL COMMENT '岗位名称',
  `code`              VARCHAR(50)  NOT NULL COMMENT '岗位编码',
  `category`          VARCHAR(50)  DEFAULT NULL COMMENT '岗位类别',
  `department`        VARCHAR(50)  DEFAULT NULL COMMENT '所属部门',
  `location`          VARCHAR(200) DEFAULT NULL COMMENT '工作地点',
  `hire_count`        INT          NOT NULL DEFAULT 0 COMMENT '招聘人数',
  `salary_range`      VARCHAR(50)  DEFAULT NULL COMMENT '薪资范围',
  `experience`        VARCHAR(30)  DEFAULT NULL COMMENT '工作经验要求',
  `education`         VARCHAR(30)  DEFAULT NULL COMMENT '学历要求',
  `description`       TEXT         DEFAULT NULL COMMENT '岗位描述',
  `status`            VARCHAR(20)  NOT NULL DEFAULT 'on' COMMENT '状态:on在招中/off已停用',
  `apply_count`       INT          NOT NULL DEFAULT 0 COMMENT '已投递人数',
  `interview_count`   INT          NOT NULL DEFAULT 0 COMMENT '面试中人数',
  `hired_count`       INT          NOT NULL DEFAULT 0 COMMENT '已录用人数',
  `hot_count`         INT          NOT NULL DEFAULT 0 COMMENT '热度计数(投递)',
  `publish_time`      DATETIME     DEFAULT NULL COMMENT '发布时间',
  `create_by`         BIGINT       DEFAULT NULL COMMENT '创建人',
  `date`              DATE         DEFAULT NULL COMMENT '创建日期',
  `timestamp`         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_position_code` (`code`),
  KEY `idx_position_status` (`status`),
  KEY `idx_position_category` (`category`),
  KEY `idx_position_department` (`department`),
  KEY `idx_position_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位管理';

-- 初始化示例数据
INSERT INTO `boss_position` (`name`, `code`, `category`, `department`, `location`, `hire_count`, `salary_range`, `experience`, `education`, `description`, `status`, `apply_count`, `interview_count`, `hired_count`, `hot_count`, `publish_time`, `date`) VALUES
('分拣打包员', 'PACK001', '分拣打包类', '仓储部', '南昌 · 青山湖区', 5, '180-260元/天', '无经验可做', '初中及以上', '负责电商包裹分拣、打包、贴单及退货件处理，按批次完成出货准备；熟练使用扫码枪与仓储WMS系统，配合库位整理与日常盘点；能适应高峰期轮班作业，遵守仓库安全操作规范。', 'on', 3, 2, 0, 56, '2025-06-26 10:30:00', CURDATE()),
('搬运装卸工', 'LOAD002', '搬运装卸类', '仓储部', '南昌 · 青山湖区', 8, '200-280元/天', '无经验可做', '不限', '负责货物搬运、装卸、码垛，配合仓库出入库作业；能适应体力劳动，遵守安全操作规范。', 'on', 5, 3, 0, 42, '2025-06-25 09:00:00', CURDATE()),
('传菜服务员', 'WAIT003', '餐饮服务类', '餐饮部', '南昌 · 高新区', 3, '150-200元/天', '不限', '不限', '负责餐厅传菜、上菜、收拾餐桌，保持用餐区域整洁；配合服务员完成日常餐饮服务工作。', 'on', 3, 1, 0, 38, '2025-06-24 11:00:00', CURDATE()),
('仓库理货员', 'WH004', '仓储理货类', '仓储部', '南昌 · 经开区', 2, '180-240元/天', '1个月以内', '初中及以上', '负责仓库货物整理、盘点、入库出库登记；熟练使用电脑基本操作，配合仓储管理系统操作。', 'on', 2, 1, 0, 25, '2025-06-23 08:30:00', CURDATE()),
('流水线操作工', 'LINE005', '生产制造类', '生产部', '南昌 · 青山湖区', 10, '160-220元/天', '不限', '不限', '负责生产线操作、产品组装、质量检验；能适应流水线作业节奏，遵守生产安全规范。', 'off', 0, 0, 0, 18, '2025-05-20 10:00:00', CURDATE())
ON DUPLICATE KEY UPDATE `timestamp` = CURRENT_TIMESTAMP;
