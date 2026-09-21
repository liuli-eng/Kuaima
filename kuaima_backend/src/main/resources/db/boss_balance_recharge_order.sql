-- 老板商户余额充值订单（JPA ddl-auto=update 会自动维护；此脚本用于已有环境显式建表）
-- 对应后端实体：com.kuaima.app.domain.boss.entity.BossBalanceRechargeOrder
CREATE TABLE IF NOT EXISTS boss_balance_recharge_order (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  order_no        VARCHAR(40)  NOT NULL                COMMENT '订单号，如 RB202609211430250001',
  boss_id         BIGINT       NOT NULL                COMMENT '老板 user.id',
  boss_name       VARCHAR(100)                         COMMENT '老板姓名（冗余）',
  company_name    VARCHAR(150)                         COMMENT '企业名称（冗余）',
  account_id      BIGINT                               COMMENT 'BossMerchantAccount.id（可为空，未开通商户号时先充值）',
  amount          DECIMAL(18,2) NOT NULL               COMMENT '充值金额（元）',
  pay_method      VARCHAR(20)  NOT NULL                COMMENT '支付方式：wechat / alipay',
  status          VARCHAR(20)  NOT NULL                COMMENT '订单状态：pending待支付 / paid已支付 / failed支付失败 / canceled已取消 / manual_pending人工待审核 / manual_confirmed人工已确认',
  pay_time        DATETIME                             COMMENT '支付完成时间',
  transaction_id  VARCHAR(64)                          COMMENT '第三方支付交易号',
  pay_qr_url      VARCHAR(500)                         COMMENT '微信/支付宝支付二维码链接（下单时返回）',
  pay_url         VARCHAR(500)                         COMMENT 'H5 支付跳转链接',
  remark          VARCHAR(500)                         COMMENT '备注（如客服确认信息）',
  operator_id     BIGINT                               COMMENT '后台审核人 id',
  operator_name   VARCHAR(100)                         COMMENT '后台审核人姓名',
  operator_time   DATETIME                             COMMENT '后台审核时间',
  operator_remark VARCHAR(500)                         COMMENT '后台审核备注（如驳回原因）',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_bbr_order_no (order_no),
  KEY idx_bbr_boss (boss_id),
  KEY idx_bbr_status (status),
  KEY idx_bbr_method (pay_method),
  KEY idx_bbr_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老板商户余额充值订单';

-- 若 boss_merchant_account 表不存在则一并创建（余额模块依赖）
CREATE TABLE IF NOT EXISTS boss_merchant_account (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  boss_id         BIGINT       NOT NULL                COMMENT '所属老板 user.id',
  account_name    VARCHAR(100)                         COMMENT '账户名称，如 晴时科技',
  subject_name    VARCHAR(150)                         COMMENT '主体全称，如 上海晴时网络科技有限公司',
  merchant_no     VARCHAR(64)                          COMMENT '商户号',
  balance         BIGINT       NOT NULL DEFAULT 0      COMMENT '账户余额（分）',
  is_default      BIT(1)       NOT NULL DEFAULT 1      COMMENT '是否默认账户',
  status          VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态：active正常 / frozen冻结',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_bma_boss (boss_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老板商户账户（余额查询）';
