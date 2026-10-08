-- 零工端“进找活群”模块升级。生产环境应在发布前执行；语句仅补结构，不伪造群或二维码数据。
ALTER TABLE social_group
    ADD COLUMN IF NOT EXISTS description VARCHAR(500) NULL COMMENT '群简介',
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'WORKER' COMMENT '可见角色: WORKER/BOSS',
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/FULL/DISABLED',
    ADD COLUMN IF NOT EXISTS member_limit INT NOT NULL DEFAULT 200 COMMENT '人数上限',
    ADD COLUMN IF NOT EXISTS created_at DATETIME NULL COMMENT '建群时间',
    ADD COLUMN IF NOT EXISTS sort INT NOT NULL DEFAULT 0 COMMENT '展示排序',
    ADD COLUMN IF NOT EXISTS deleted BIT NOT NULL DEFAULT 0 COMMENT '是否删除';

CREATE TABLE IF NOT EXISTS social_group_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    source VARCHAR(100) NULL,
    confirmed BIT NOT NULL DEFAULT 0,
    joined_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_social_group_member (group_id, user_id),
    KEY idx_social_group_member_user (user_id),
    CONSTRAINT fk_social_group_member_group FOREIGN KEY (group_id) REFERENCES social_group (id),
    CONSTRAINT fk_social_group_member_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社群加入确认记录';
