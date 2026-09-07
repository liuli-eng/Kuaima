# 结算与岗位接口 Bug 清单

> 整理范围：老板端岗位与结算页面、零工端结算查询接口。
>
> 当前项目目录：`kuaima-uniapp`

## Bug 总览

| 编号 | 类型 | 页面/接口 | 优先级 | 状态 |
| --- | --- | --- | --- | --- |
| BUG-001 | 缺少接口对接 | `pages/boss/all-jobs` | 高 | 待处理 |
| BUG-002 | 缺少接口对接 | `pages/boss/suspend-settle` | 高 | 待后端补充接口 |
| BUG-003 | 接口业务异常 | `GET /settle/worker/1` | 高 | 待排查后端结算链路 |

## BUG-001：老板端全部岗位页面缺少真实数据接口对接

### 页面

```text
pages/boss/all-jobs
```

### 问题描述

页面存在岗位列表、岗位选择和进入编辑流程，但当前页面没有完整使用后端岗位列表接口加载数据，仍存在静态数据或依赖本地页面状态的情况。

### 现有后端能力

后端已有岗位列表接口：

```http
GET /boss/order?page=0&size=20&type=&status=&title=
```

后端控制器：

```text
BossController.listOrders()
```

### 复现步骤

1. 进入老板端首页。
2. 点击“全部岗位”或“全部招工”。
3. 检查岗位列表数据来源。
4. 修改或新增岗位后返回列表。

### 实际结果

- 页面无法稳定展示当前老板在后端保存的全部岗位。
- 列表可能依赖静态演示数据、本地缓存或上一页面传递的数据。
- 新增、编辑、删除岗位后，列表数据可能不能及时同步。

### 预期结果

- 页面进入时调用后端岗位列表接口。
- 只展示当前登录老板创建的岗位。
- 支持岗位状态、结算方式和关键词筛选。
- 新增、编辑、删除或状态变更后刷新列表。
- 接口失败时展示错误状态或空状态，不展示假数据。

### 建议处理

前端对接：

```http
GET /boss/order?page=0&size=20&status=&type=&title=
```

建议后端确认：

1. `GET /boss/order` 是否根据当前登录用户自动过滤 `createBy`。
2. 如果没有自动过滤，新增老板专用接口：

   ```http
   GET /boss/orders/mine?page=0&size=20&status=&type=&title=
   ```

3. 返回字段至少包括：`id`、`orderTitle`、`orderStatus`、`type`、`salary`、`orderNum`、`startTime`、`endTime`、`address`。

## BUG-002：老板端待结算页面缺少全部订单结算接口

### 页面

```text
pages/boss/suspend-settle
```

### 问题描述

页面需要展示当前老板所有订单中的待结算报名记录，但当前只使用静态订单数据，没有调用后端接口。

页面目前包含：

- 待结算总金额
- 待结算订单数量
- 涉及零工数量
- 订单选择
- 批量结算入口

这些数据目前均为前端演示数据。

### 现有后端能力

后端已有单订单结算相关接口：

```http
GET  /settle/order/{orderId}
POST /settle?itemId={itemId}&workDays={workDays}
POST /settle/{settlementId}/pay
```

但没有“当前老板全部订单待结算数据”接口。

### 复现步骤

1. 进入老板端“待结算”页面。
2. 查看订单数量、零工数量和金额。
3. 对比数据库中的老板订单、已完成报名记录和结算记录。
4. 点击“结算所选”。

### 实际结果

- 页面显示固定的演示订单和金额。
- 页面无法获取当前老板全部订单的真实待结算记录。
- 批量结算只携带静态订单 ID 和金额，未创建真实结算单。
- 确认付款页面目前也不能可靠关联真实 `settlementId`。

### 预期结果

- 查询当前登录老板所有订单。
- 筛选报名状态为“已完成”的记录。
- 排除已经“待支付”或“已支付”的结算记录。
- 按订单聚合展示待结算金额和零工信息。
- 批量结算时创建真实结算单，并使用真实结算单 ID 完成支付。

### 建议新增后端接口

#### 1. 查询老板全部待结算数据

```http
GET /boss/settlements/pending
```

建议从 JWT 获取当前老板 ID，不由前端传入 `bossId`。

建议返回：

```json
{
  "code": 200,
  "data": {
    "totalAmount": 360000,
    "totalWorkers": 8,
    "totalOrders": 5,
    "orders": [
      {
        "orderId": 10,
        "orderTitle": "电商分拣打包工",
        "workDate": "2026-08-21",
        "amount": 80000,
        "status": "待结算",
        "workerCount": 3,
        "items": [
          {
            "itemId": 101,
            "workerId": 2001,
            "workerName": "张师傅",
            "workDays": 1,
            "wage": 18000,
            "settlementId": null,
            "settlementStatus": null
          }
        ]
      }
    ]
  }
}
```

筛选规则：

1. 订单创建人是当前登录老板。
2. 报名记录状态为“已完成”。
3. 不存在状态为“待支付”或“已支付”的结算单。
4. 已部分结算的订单标记为“部分结算”。
5. 金额统一使用“分”，避免前后端金额单位混乱。

#### 2. 批量创建结算单

```http
POST /boss/settlements/batch
```

请求示例：

```json
{
  "items": [
    { "itemId": 101, "workDays": 1 },
    { "itemId": 102, "workDays": 2 }
  ]
}
```

#### 3. 批量支付结算单

```http
POST /boss/settlements/batch/pay
```

请求示例：

```json
{
  "settlementIds": [501, 502]
}
```

## BUG-003：零工结算接口没有返回订单完成后的结算数据

### 接口

```http
GET /settle/worker/1
```

### 问题描述

订单完成后，调用零工结算列表接口没有查到对应结算记录，或返回空数组。

### 后端实现位置

```text
SettlementController.listByWorker()
SettlementService.listByWorker()
SettlementRespository.findByWorkerIdOrderByIdDesc()
```

当前查询逻辑实际是：

```java
settlementRepository.findByWorkerIdOrderByIdDesc(userId)
```

该接口只查询 `boss_settlement.worker_id = userId` 的记录。

### 复现步骤

1. 零工报名岗位。
2. 完成岗位工作，并将报名记录状态变为“已完成”。
3. 老板发起结算并完成支付。
4. 查询：

   ```http
   GET /settle/worker/1
   ```

5. 检查是否返回对应结算单。

### 实际结果

- 订单完成后不一定自动产生结算数据。
- 仅将报名记录状态改为“已完成”，不会自动创建 `Settlement`。
- 只有老板调用 `POST /settle?itemId=&workDays=` 后，才会生成“待支付”结算单。
- 只有调用 `POST /settle/{id}/pay` 后，结算单才会变成“已支付”。
- 如果 `worker_id` 写入错误、使用了错误用户 ID，零工接口也会返回空结果。

### 重点排查项

1. 是否真的调用了：

   ```http
   POST /settle?itemId={itemId}&workDays={workDays}
   ```

2. `itemId` 对应报名记录的 `user_id` 是否为 `1`。
3. 结算单 `boss_settlement.worker_id` 是否正确写入报名记录的 `user_id`。
4. 老板和零工是否使用了不同的数据库环境。
5. 查询请求是否携带有效 JWT。
6. 订单完成不等于自动结算，当前后端没有“完成后自动创建结算单”的逻辑。

### 建议处理

后端需要明确业务规则：

- 如果“订单完成”只代表报名记录完成，保持现有流程，由老板主动发起结算。
- 如果要求订单完成后自动生成结算单，则在完成状态流转后调用结算服务，创建“待支付”结算单。
- 如果要求订单完成后直接到账，则还需要自动调用支付结算逻辑，但不建议跳过老板确认。

建议增加校验接口或日志：

```http
GET /settle/{id}/detail
GET /settle/order/{orderId}
```

并检查数据库：

```sql
SELECT * FROM boss_order_item WHERE id = {itemId};
SELECT * FROM boss_settlement WHERE worker_id = 1 ORDER BY id DESC;
```

## 验收标准

### `pages/boss/all-jobs`

- 页面展示真实岗位列表。
- 只展示当前老板自己的岗位。
- 新增、编辑、删除后列表数据同步更新。
- 无接口或接口失败时不展示静态假数据。

### `pages/boss/suspend-settle`

- 页面展示当前老板全部订单的真实待结算数据。
- 金额、订单数、零工数与后端统计一致。
- 批量结算生成真实结算单。
- 支付后待结算列表正确移除，零工端能查询到结算记录。

### `/settle/worker/{userId}`

- 结算单的 `workerId` 与报名记录 `userId` 一致。
- 老板发起结算后，零工接口能查询到“待支付”记录。
- 老板完成支付后，零工接口能查询到“已支付”记录。
- 订单仅完成但未发起结算时，接口返回空是符合当前后端设计的。
