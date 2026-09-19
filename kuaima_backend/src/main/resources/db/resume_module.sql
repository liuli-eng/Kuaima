-- 简历库模块：简历、简历经历（教育/工作/项目）、导入记录
-- 重复执行安全（IF NOT EXISTS）

-- 1. 简历表
CREATE TABLE IF NOT EXISTS `boss_resume` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `boss_id` BIGINT NOT NULL COMMENT '所属老板用户ID',
  `enterprise_id` BIGINT DEFAULT NULL COMMENT '企业ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '关联工人用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
  `age` INT DEFAULT NULL COMMENT '年龄',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `city` VARCHAR(100) DEFAULT NULL COMMENT '居住地',
  `id_card` VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
  `position` VARCHAR(50) DEFAULT NULL COMMENT '意向岗位',
  `job_category` VARCHAR(30) DEFAULT NULL COMMENT '职位类型（分拣打包/搬运装卸/餐饮服务等）',
  `education` VARCHAR(20) DEFAULT NULL COMMENT '学历',
  `experience` VARCHAR(30) DEFAULT NULL COMMENT '经验描述（如2年经验）',
  `expected_salary` VARCHAR(50) DEFAULT NULL COMMENT '期望薪资（如180-260元/天）',
  `work_location` VARCHAR(50) DEFAULT NULL COMMENT '期望工作地点',
  `status` VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT 'NEW新/PENDING待处理/VIEWED已查看/SENT已投递',
  `favorite` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否收藏',
  `source` VARCHAR(20) NOT NULL DEFAULT 'APPLY' COMMENT 'APPLY投递/IMPORT导入',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT '简历文件地址',
  `create_by` BIGINT DEFAULT NULL,
  `date` DATE DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_boss_status` (`boss_id`,`status`),
  KEY `idx_resume_category` (`job_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历库-简历';

-- 2. 简历经历表
CREATE TABLE IF NOT EXISTS `boss_resume_experience` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resume_id` BIGINT NOT NULL COMMENT '简历ID',
  `type` VARCHAR(20) NOT NULL COMMENT 'EDU教育/WORK工作/PROJECT项目',
  `title` VARCHAR(100) NOT NULL COMMENT '标题（学校名/职位/项目名）',
  `org` VARCHAR(100) DEFAULT NULL COMMENT '机构（专业学历/公司/角色）',
  `start_date` VARCHAR(20) DEFAULT NULL COMMENT '开始时间（如2023.07）',
  `end_date` VARCHAR(20) DEFAULT NULL COMMENT '结束时间（如2025.06/至今）',
  `description` TEXT COMMENT '描述',
  `tags` VARCHAR(200) DEFAULT NULL COMMENT '亮点标签，逗号分隔',
  `create_by` BIGINT DEFAULT NULL,
  `date` DATE DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_exp_resume` (`resume_id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历库-经历';

-- 3. 简历导入记录表
CREATE TABLE IF NOT EXISTS `boss_resume_import` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `boss_id` BIGINT NOT NULL COMMENT '老板用户ID',
  `enterprise_id` BIGINT DEFAULT NULL COMMENT '企业ID',
  `file_name` VARCHAR(200) NOT NULL COMMENT '文件名',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件地址',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS导入成功/FAILED解析失败',
  `resume_id` BIGINT DEFAULT NULL COMMENT '解析生成的简历ID',
  `fail_reason` VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
  `create_by` BIGINT DEFAULT NULL,
  `date` DATE DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_import_boss` (`boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历库-导入记录';
