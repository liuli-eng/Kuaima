# 零工端奖励金接口文档

## 通用约定

- Base Path：`/worker/rewards`
- 认证：`Authorization: Bearer <JWT>`，后端只从 JWT 的 `LoginUser.id` 取当前用户，请求参数不接收 `userId`。
- 登录角色：必须是零工端角色 `USER`。已完成企业认证但切换到零工端登录的用户，仍按同一个 `userId` 读取奖励金资产。
- 金额单位：本组接口的请求和响应金额均为**分**；服务层在资产边界完成与既有存储表示的换算。
- 统一响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

业务校验失败由统一异常处理返回 `code=400`，未认证或角色错误返回 `403`，数据不存在返回 `404`。

## 1. 奖励金账户概览

```http
GET /worker/rewards/overview
Authorization: Bearer <JWT>
```

### 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| balance | integer | 奖励金总余额，单位分 |
| withdrawableBalance | integer | 可提现奖励金，`balance - frozenAmount`，单位分 |
| minimumWithdrawAmount | integer | 最低提现金额，来自 `admin_setting`，单位分 |
| withdrawEnabled | boolean | 当前用户当前时刻是否允许发起提现 |
| withdrawDisabledReason | string/null | 不允许提现的原因 |

可能的原因包括：提现开关关闭、未实名、未绑定微信、可提现余额低于最低金额。

### 示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "balance": 2000,
    "withdrawableBalance": 2000,
    "minimumWithdrawAmount": 1000,
    "withdrawEnabled": true,
    "withdrawDisabledReason": null
  }
}
```

## 2. 奖励金明细

```http
GET /worker/rewards/records?page=0&size=20&type=ALL
Authorization: Bearer <JWT>
```

### 查询参数

| 参数 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- |
| page | 否 | `0` | 页码，从 0 开始 |
| size | 否 | `20` | 每页条数，范围 1-100 |
| type | 否 | `ALL` | `ALL` 全部；`INCOME` 收入；`WITHDRAW` 提现 |

查询在数据库层分页并按 `createdAt desc, id desc` 排序，只查询 JWT 用户流水。

### 记录字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | integer | 流水 ID |
| type | string | `INCOME` 或 `WITHDRAW` |
| bizType | string | 业务类型，如 `INVITE_FIRST_ORDER`、`WECHAT_WITHDRAW`、`WITHDRAW_REFUND` |
| title | string | 展示标题 |
| description | string/null | 展示描述 |
| amount | integer | 收入为正数，提现为负数，单位分 |
| balanceAfter | integer | 该流水后的奖励金余额，单位分 |
| status | string | `PENDING`、`SUCCESS`、`FAILED`；历史无状态流水按 `SUCCESS` 返回 |
| createdAt | string | 创建时间 |

### 分页字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| records | array | 当前页数据 |
| total | integer | 总记录数 |
| page | integer | 当前页码 |
| size | integer | 每页条数 |
| hasMore | boolean | 是否还有下一页 |

## 3. 获取方式与规则

```http
GET /worker/rewards/rules
Authorization: Bearer <JWT>
```

### 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| minimumWithdrawAmount | integer | 最低提现金额，单位分，来自后台配置 |
| withdrawChannel | string | 当前配置提现渠道，当前为 `WECHAT` |
| earningMethods | array | 获取方式列表，来自 `reward_earning_rule` |

### earningMethods 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | string | 稳定规则编码 |
| name | string | 规则名称 |
| description | string/null | 规则说明 |
| rewardAmount | integer/null | 固定奖励金额，单位分；不固定时为空 |
| actionType | string | `NAVIGATE` 跳转、`SHARE` 分享、`NONE` 无动作 |
| actionPath | string/null | 小程序跳转路径 |
| enabled | boolean | 是否启用 |

## 4. 奖励金提现

```http
POST /worker/rewards/withdraw
Authorization: Bearer <JWT>
Idempotency-Key: <client-unique-key>
Content-Type: application/json
```

### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| amount | integer | 是 | 提现金额，单位分，必须大于 0 |
| channel | string | 是 | 当前仅支持 `WECHAT` |

```json
{
  "amount": 1000,
  "channel": "WECHAT"
}
```

`Idempotency-Key` 必填，长度 1-100。后端保存为用户前缀后的完整幂等键；同一用户同一键重复请求会返回同一笔单据；不同用户使用相同客户端键不会互相冲突。

### 成功受理响应

```json
{
  "code": 200,
  "message": "提现申请已提交",
  "data": {
    "withdrawId": 90001,
    "amount": 1000,
    "status": "PENDING",
    "balance": 1000,
    "createdAt": "2026-09-21T18:30:00"
  }
}
```

### 提现校验

按顺序校验：

1. JWT 用户存在；
2. 提现开关开启；
3. 已完成实名认证；
4. 已绑定微信 openid；
5. 金额不低于后台配置的最低提现金额；
6. `balance - frozenAmount` 足够支付本次提现；
7. 幂等键未与其他金额、渠道或用户冲突。

### 事务和资金安全

- 提交时锁定用户行和奖励金账户行，账户同时带乐观锁版本。
- 建立唯一幂等键并扣减奖励金余额。
- 写入 `WECHAT_WITHDRAW` 提现流水，初始状态 `PENDING`。
- 数据库事务提交后调用微信商家转账 `/v3/transfer/batches`。
- 受理成功保存商家批次单号、商家明细单号、微信批次单号和微信响应。
- 发起失败会独立提交失败状态、退回余额，并写 `WITHDRAW_REFUND` 收入流水；接口返回 `code=502`。
- 微信受理后进入 `PENDING`；调度任务每分钟查询超过 1 分钟的待处理单。终态成功会把提现流水更新为 `SUCCESS`；终态失败会退回余额并把原流水置为 `FAILED`，同时生成退回收入流水。
- 微信接口错误信息不包含 openid、身份证号、银行卡号、API Key 或商户私钥。

## 后台配置

配置保存在 `admin_setting`，由初始化器首次写入，后续修改不被覆盖：

| key | 默认值 | 说明 |
| --- | --- | --- |
| `reward.withdraw.minimumAmount` | `1000` | 最低提现金额，单位分 |
| `reward.withdraw.enabled` | `true` | 提现开关 |
| `reward.withdraw.disabledReason` | 空字符串 | 提现关闭原因 |
| `reward.withdraw.channel` | `WECHAT` | 提现渠道 |

规则保存在 `reward_earning_rule`，初始包含：

| code | rewardAmount | actionType | actionPath |
| --- | --- | --- | --- |
| `INVITE_FIRST_ORDER` | `800` | `NAVIGATE` | `/pages/worker/invite` |
| `INCOME_SHARE` | `200` | `SHARE` | null |
| `ACTIVITY` | null | `NONE` | null |

## 存储和兼容

为避免重复建设账户并保持老板端现有接口兼容，本功能复用：

- `reward_account`
- `reward_flow`
- `reward_withdrawal`

新增字段包括账户冻结金额和乐观锁版本、流水状态/描述/幂等键、提现渠道和微信转账结果等。迁移 SQL：

```text
src/main/resources/db/worker_reward_module.sql
```

该 SQL 需要在升级后端前执行一次。执行前先备份数据库，并确认新增列尚不存在；表内历史数据不做归属猜测或自动改写。
