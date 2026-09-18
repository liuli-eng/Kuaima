-- 管理后台学堂内容表。Hibernate ddl-auto=update 会补建表，此脚本用于环境初始化和固定课程槽位。
CREATE TABLE IF NOT EXISTS academy_simulate_video (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    duration INT NOT NULL DEFAULT 0,
    size BIGINT NOT NULL DEFAULT 0,
    ext VARCHAR(10) NOT NULL DEFAULT '',
    sort INT NOT NULL DEFAULT 0,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    learners BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS academy_quiz (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    score INT NOT NULL,
    stem TEXT NOT NULL,
    options TEXT NOT NULL,
    answer VARCHAR(100) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    KEY idx_academy_quiz_type_sort (type, sort, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS academy_lesson (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lesson_key VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    video VARCHAR(500) NULL,
    duration INT NULL,
    size BIGINT NULL,
    ext VARCHAR(10) NULL,
    learners BIGINT NOT NULL DEFAULT 0,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    uploaded_at DATETIME NULL,
    UNIQUE KEY uk_academy_lesson_key (lesson_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 新手课程固定槽位由服务端维护，前端不再内置课程定义。
INSERT INTO academy_lesson(id, lesson_key, title, description, learners, enabled)
VALUES
    (1, 'register', '新人注册与实名认证', '完成账号注册、实名认证及资料完善', 0, b'1'),
    (2, 'find', '如何浏览和报名岗位', '筛选岗位、查看详情、报名接单', 0, b'1'),
    (3, 'work', '接单后如何工作', '联系老板、准时到岗、打卡完工', 0, b'1'),
    (4, 'withdraw', '如何提现？', '绑定本人实名账户、发起提现与到账时间规则说明', 0, b'1'),
    (5, 'credit', '信用分有什么用？', '信用分等级、高低分对接单权益的影响与提分技巧', 0, b'1')
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description);
