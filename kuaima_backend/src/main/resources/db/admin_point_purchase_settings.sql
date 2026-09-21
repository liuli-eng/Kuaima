-- point_package 是后台与老板端购买积分共用的唯一套餐表；price 单位为人民币元。
CREATE TABLE IF NOT EXISTS point_package (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) NOT NULL, sub VARCHAR(100), points BIGINT NOT NULL, price BIGINT NOT NULL,
 original_price VARCHAR(50), save_text VARCHAR(50), rec BOOLEAN NOT NULL DEFAULT FALSE, sort INT NOT NULL DEFAULT 0,
 enabled BOOLEAN NOT NULL DEFAULT TRUE, operator_id BIGINT, operator_name VARCHAR(100), created_at DATETIME, updated_at DATETIME,
 INDEX idx_point_package_points(points), INDEX idx_point_package_price(price)
);
ALTER TABLE point_package MODIFY COLUMN price DECIMAL(10,2) NOT NULL;
CREATE TABLE IF NOT EXISTS point_exchange_rule (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL, sub VARCHAR(100), icon VARCHAR(100), icon_color VARCHAR(20),
 rate VARCHAR(100) NOT NULL, rule_limit VARCHAR(100), cond VARCHAR(200), rule_code VARCHAR(50) UNIQUE, formula VARCHAR(100), sort INT NOT NULL DEFAULT 0,
 enabled BOOLEAN NOT NULL DEFAULT TRUE, operator_id BIGINT, operator_name VARCHAR(100), created_at DATETIME, updated_at DATETIME,
 INDEX idx_point_exchange_enabled_sort(enabled,sort)
);
CREATE TABLE IF NOT EXISTS point_earn_rule (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL, sub VARCHAR(100), icon VARCHAR(100), icon_color VARCHAR(20),
 rate VARCHAR(100) NOT NULL, rule_limit VARCHAR(100), cond VARCHAR(200), rule_code VARCHAR(50) UNIQUE, formula VARCHAR(100), sort INT NOT NULL DEFAULT 0,
 enabled BOOLEAN NOT NULL DEFAULT TRUE, operator_id BIGINT, operator_name VARCHAR(100), created_at DATETIME, updated_at DATETIME,
 INDEX idx_point_earn_enabled_sort(enabled,sort)
);
