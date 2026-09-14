# 快马日结老板端「诚意分/雇主星级」模块需求规格（spec）

## 一、问题（Problem）

当前仓库已存在 3 个与「星级/信用/老板统计」相关的接口，但其语义与业务用途均与老板诚意分（雇主星级）不兼容：

1. **`GET /star-level/{userId}`** — 通用星级实体 `UserStarLevel(level, progress)`，语义未定，仅有「1 星/2 星…」等级编号与百分比进度，不包含星级名称、分数阈值、权益、周期、各维度比例等老板诚意分需要展示的核心字段，也不区分用户角色。
2. **`GET /user/{id}/credit`** — 零工信用分 `User.creditScore + CreditFlow(userId, delta, reason, bizType)`，设计目标为工人维度的完单/迟到/取消等行为，不能直接复用作老板诚意分。
3. **`GET /boss/stats?userId={userId}`** — 老板首页 4 项大盘聚合：总订单数、招工中数量、报名总人数、已结算金额。不涉及诚意分/星级/权益/比例维度，但诚意分计算需要复用其中的订单、报名、结算等基础数据源。

老板端 `pages/boss/creditor-score` 页面需要**一套独立的、以老板身份为主体的诚意分评分体系**（诚意分 `integrityScore`、星级 `starLevel`、权益 `benefits`、周期、到达完单率、结算速度、评价等多维度指标，以及可追溯的诚意分流水记录和规则/权益说明）。前端页面无法通过拼接上述 3 个接口获得正确数据。

**目标用户**：老板（`User.role == BOSS`）进入诚意分/雇主星级页查看个人信用表现、权益、明细流水及规则。

## 二、目标（Goals）

1. 提供 3 个 REST 接口，支撑老板端诚意分页面的 3 个场景：
   - **总览**：`GET /boss/creditor-score/{userId}` — 老板诚意分概览 + 星级 + 权益 + 各维度比例 + 周期。
   - **流水**：`GET /boss/creditor-score/{userId}/flows?page=&size=` — 诚意分变动明细分页。
   - **规则**：`GET /boss/creditor-score/rules` — 星级阈值、权益列表、评分规则说明（全局静态）。
2. 老板诚意分与工人信用分（`User.creditScore` / `CreditFlow`）**物理隔离**，互不复用实体、字段或枚举，不污染现有工人信用体系。
3. 接口必须进行**越权校验**：请求中的 `{userId}` 必须等于当前登录 JWT 中解析的用户 ID，否则返回 403，禁止查看其他老板的诚意分。
4. 诚意分流水必须**可追溯、可持久化**，通过独立的 `BossIntegrityFlow` 表记录每次变动（变动值、变动后分值、类型、关联订单、时间）；不得每次查询时在内存临时拼接或依赖不具备确定性的结果。
5. 比例字段统一按「数字百分比」返回（如 `91.5`，范围 0~100），不返回 `%` 字符串；样本不足（到达样本 < 4 单）时返回 `null`，由前端展示「暂不统计」。
6. 与三个现有接口保持**清晰关系边界**，不重复实现已有功能：
   - 不修改 `/star-level`、`/user/{id}/credit`、`/boss/stats` 既有行为。
   - `BossIntegrityScoreService` 中需要的老板订单/报名/结算统计，复用 `BossOrderRespository`、`BaseOrderItemRespository`、`SettlementRespository` 的查询能力，但不直接在 Controller 内拼装。

## 三、非目标（Non-Goals）

1. **不实现**「诚意分事件触发写入」（如订单完成/结算支付/取消/评价发生时自动计算并写入流水）。本次模块只实现：
   - 规则/权益静态接口；
   - 总览接口 — 基于周期内订单、到达、结算、取消记录聚合计算当前诚意分（首次可从 0 起步，并把聚合结果同步持久化为最新快照）；
   - 流水接口 — 基于 `BossIntegrityFlow` 表分页查询；若当前阶段未接入事件，允许流水为空数组 `[]`（按验收标准 AC-10 执行）。
   *诚意分实时写入将作为后续迭代，在订单/结算/评价业务模块接入时完成。*
2. **不新增零工评价老板的实体/接口**。当前仓库尚未实现评价表，`positiveReviewRate / negativeReviewCount / unreviewedCount` 按规范返回 0 或 null（见 AC-11），不越界实现评价模块。
3. **不修改** 现有 `UserStarLevel` 实体、`CreditFlow` 实体、`User.creditScore` 字段及其仓储/接口行为。
4. **不修改** 已有 `SecurityConfig` 的放行白名单（3 个接口均需鉴权，使用现有 JWT 过滤器即可）。

## 四、功能需求（Functional Requirements）

### FR-1 总览接口 GET /boss/creditor-score/{userId}

- 校验 `{userId}` == 当前登录 JWT `uid`，不等则返回 HTTP 403 `Result.error(403, "无权限查看其他用户诚意分")`。
- 校验用户角色 == `BOSS`，否则返回 HTTP 400 `Result.error(400, "诚意分仅支持老板身份")`。
- 计算当前周期（`periodStart` ~ `periodEnd`）：规则为**滚动 90 天（3 个月）**，`periodEnd = today`，`periodStart = today - 89 天`（闭区间共 90 天）。周期每天滚动，不按自然月切换。
- `integrityScore`：基于周期内数据聚合（到达完单 +6/+3/-10、结算 1h/24h 加分、超 24h 扣分、取消扣分；详见 FR-3 的 `rules[].items[].changeScore` 权重表）。**不得读取 `User.creditScore`**。
- `scoreRanges`：长度固定为 5 的数组 `[0, 40, 100, 200, 600]`，对应 0~4 星的区间下界（含），其中第 4 星上限取 `∞`。
- `starLevel`、`starName`、`starDescription`：
  | 区间 | starLevel | starName | starDescription |
  |---|---|---|---|
  | integrityScore == 0 | 0 | 零星雇主 | 暂无诚意数据 |
  | 1 ~ 39 | 1 | 一星雇主 | 多完单·不飞单·结算快·好评多 |
  | 40 ~ 99 | 2 | 二星雇主 | 准时发薪·按时上岗 |
  | 100 ~ 199 | 3 | 三星雇主 | 结算高效·招工靠谱 |
  | 200 ~ 599 | 4 | 四星雇主 | 优质雇主·结算超快·深受好评 |
  | ≥ 600 | 5 | 五星雇主 | 官方认证·标杆雇主·顶级权益 |
- `levelProgress`：当前星级区间内进度百分比（0~100，保留 1 位小数）。
  - 零星 (L0)：固定 0；
  - 五星 (L5)：固定 100；
  - 其他等级：`(integrityScore - minScore) / (maxScore + 1 - minScore) * 100`，其中 L1=(1,40), L2=(40,100), L3=(100,200), L4=(200,600)。
- `benefitCount`、`benefits`：从规则表（FR-3）按 `starLevel` 匹配权益列表，数量即 `benefitCount`。L0 权益含「禁止招工」，其他等级当前为空数组（后续扩展）。
- **到达完单率**：周期内有「到达」记录的报名样本中，完单（`finishDate != null`且未取消）的比例。样本数 `arrivalSampleCount < 4` 时 `arrivalCompletionRate = null`。
- **结算速度**：周期内所有 `已支付` 结算单，计算 `payTime - 订单完成时间(finishDate)` 落在 1h / 24h / >24h 区间的比例。如该周期内无结算单，`settlementWithin1hRate = settlementWithin24hRate = null`。
- **评价**：`positiveReviewRate / negativeReviewCount / unreviewedCount` 暂返回 `null / 0 / 0`（评价实体未落地前的占位行为）。
- **订单计数**：`completedOrderCount` 周期内完单订单数，`cancelledOrderCount` 周期内取消订单数（`orderStatus = "取消招工"` 或报名被取消）。
- 所有聚合计算完成后，应在同一事务中**持久化**老板的诚意分快照（实体 `BossIntegrityScore`：`userId / score / starLevel / periodStart / periodEnd / lastCalcAt`），保证下次查询有缓存且流水可关联。

### FR-2 流水接口 GET /boss/creditor-score/{userId}/flows?page=0&size=20

- 越权/角色校验同 FR-1。
- 查询 `BossIntegrityFlow` 表按 `occurredAt desc` 分页，返回字段：
  - `id`：流水主键；
  - `occurredAt`：发生时间（LocalDateTime，ISO-8601）；
  - `type`：枚举字符串，取值见 FR-3；
  - `title` / `description`：前端展示标题与订单号说明；
  - `changeScore`：整数分值，正负号；
  - `scoreAfter`：变动后分值。
- 分页字段：`page`（0-based）、`size`、`total`（总数）、`hasNext`（是否还有下一页）。
- 无数据：返回 `content=[]`、`total=0`、`hasNext=false`、不缺失字段。

### FR-3 规则接口 GET /boss/creditor-score/rules（全局静态，无 userId，不需鉴权）

- `levels[]`：星级阈值与权益，与 FR-1 表格一致（L0-L5）。
- `rules[]`：评分规则分组。至少包含：
  1. 到达完单率 `ARRIVAL_COMPLETED`（100% / 90% → +6 / +3；<70% → -10）；
  2. 结算速度：`SETTLE_WITHIN_1H`（1h 内 +5）、`SETTLE_WITHIN_24H`（24h 内 +3）、`SETTLE_OVER_24H`（>24h -8）；
  3. 取消订单 `ORDER_CANCELLED`（老板取消 -5）；
  4. 评价（占位，规则展示即可）：`POSITIVE_REVIEW` +5 / `NEGATIVE_REVIEW` -15。
- 每个 `rule.items[]` 包含 `condition / changeScore / label` 三个字段。

### FR-4 诚意分流水事件类型枚举（`BossIntegrityFlow.type` & 规则）

```
ARRIVAL_COMPLETED       到达并完单
ARRIVAL_NOT_COMPLETED   到达未完结
SETTLE_WITHIN_1H        1 小时内结算
SETTLE_WITHIN_24H       24 小时内结算
SETTLE_OVER_24H         超过 24 小时结算
POSITIVE_REVIEW         零工好评
NEGATIVE_REVIEW         零工差评
ORDER_CANCELLED         取消订单
```

## 五、非功能需求（Non-Functional Requirements）

1. **性能**：总览接口单次查询在 1 万订单规模下 P95 ≤ 500ms，通过「周期条件下推 SQL（WHERE finishDate/payTime BETWEEN periodStart AND periodEnd）+ `BossIntegrityScore` 缓存快照 + `BossIntegrityFlow` 独立分页」保证。
2. **幂等**：同一天重复调用总览接口不重复写入流水（流水写入在后续事件阶段才需要；当前阶段流水允许为空，快照 `lastCalcAt` 同一天只更新一次）。
3. **可观测**：关键路径使用日志（聚合计算耗时、越权拒绝记录、无效角色拒绝、空数据占位记录）。
4. **Swagger/OpenAPI**：3 个接口必须有 `@Operation` / `@Tag` 注解，请求参数与返回字段明确。
5. **字段完整性**：所有 JSON 返回必须包含规定字段，空值用 `null` 或 `0` 占位，不得缺失字段键。
6. **与现有解耦**：新增实体、Repository、Service、Controller、DTO 全部在独立包路径 `com.kuaima.app.domain.bossintegrity` 或 `controller/boss` 下，不修改现有 CreditFlow / UserStarLevel / BossStats 源码（除越权校验用到的 `SecurityAuditorAware` 能力、构造器注入依赖已有 Repository 外）。

## 六、约束、依赖、假设（Constraints / Dependencies / Assumptions）

1. **数据库**：现有项目使用 JPA + Hibernate，需提供 `BossIntegrityScore` 和 `BossIntegrityFlow` 两张表的 DDL 迁移 SQL 文件，路径 `src/main/resources/db/`，文件名前缀使用递增可读语义（如 `boss_integrity_schema.sql`），不使用时间戳。
2. **越权校验依赖 JWT**：使用现有 `JwtUtil.getUserId(token)` 与 Spring Security `Authentication.getPrincipal()`（`LoginUser.id()`）一致；Controller 内拿到 `Authentication` 并在方法开头校验。
3. **周期计算**：使用服务器时区（Asia/Shanghai）的 `LocalDate` 计算 `periodStart/periodEnd`，不依赖数据库时区。
4. **到达完单率样本**：到达记录由「报名记录 `BaseOrderItem.hireDate` 非空」近似（当前仓库没有独立的「到达签到」表），与用户需求的「样本数不足 4 返回 null」兼容。若未来上线更精确的到达表，只需替换数据源方法即可。
5. **结算时间差**：`Settlement.payTime`（已支付时）减去订单完成时间基准 `BaseOrderItem.finishDate`；若 `finishDate` 缺失则回退到 `BossOrder.endTime`，保证分母不为 0。
6. **评价相关字段**：因评价实体尚未实现，`positive/negative/unreviewed` 按占位值返回（见 AC-11），不抛异常、不缺失字段。

## 七、开放问题（Open Questions）

| # | 问题 | 暂定决策（若用户未回复则按此执行） |
|---|---|---|
| OQ-1 | 诚意分计算后是否需要管理员后台手动调整入口？ | 本次不含管理后台调分接口，仅保留 Service 层方法供后续扩展。 |
| OQ-2 | 零星权益「禁止招工」是否实际在招工发布接口中拦截？ | 本次仅展示权益文本，不做发布拦截（后续与招工流程一并规划）。 |
| OQ-3 | 滚动 90 天是否符合实际产品期望？ | 本次按 90 天实现，代码中抽取常量 `PERIOD_DAYS = 90`，后续改配置仅需改一处。 |

## 八、验收标准（Acceptance Criteria）

> AC 类型仅允许 `rule`（可客观验证的通过条件）或 `rubric`（0-2 等评价维度）。

### AC-1 rule 越权校验
调用方 JWT 的 `uid` ≠ 路径 `{userId}` 时，总览与流水接口均返回 HTTP 403，响应体 `code=403`，`message` 为明确的越权拒绝文案，且未返回任何诚意分数据。

### AC-2 rule 角色校验
查询用户 `role != BOSS` 时，总览与流水接口返回 HTTP 400，`message="诚意分仅支持老板身份"`。

### AC-3 rule 总览字段完整性
调用成功的总览响应 JSON **包含且字段名完全一致**于：`userId, starLevel, starName, starDescription, integrityScore, periodStart, periodEnd, scoreRanges, levelProgress, benefitCount, benefits, arrivalCompletionRate, arrivalSampleCount, settlementWithin1hRate, settlementWithin24hRate, positiveReviewRate, negativeReviewCount, unreviewedCount, completedOrderCount, cancelledOrderCount`，无缺键。

### AC-4 rule 星级映射（边界值）
- `integrityScore = 0` → `starLevel=0, starName="零星雇主"`；
- `integrityScore = 39` → `starLevel=1`；
- `integrityScore = 40` → `starLevel=2`；
- `integrityScore = 99` → `starLevel=2`；
- `integrityScore = 100` → `starLevel=3`；
- `integrityScore = 199` → `starLevel=3`；
- `integrityScore = 200` → `starLevel=4`；
- `integrityScore = 599` → `starLevel=4`；
- `integrityScore = 600` → `starLevel=5, levelProgress=100`。

### AC-5 rule levelProgress 计算
- L1（score=1→40，共 40 档）：`score=1 → 0.0`，`score=40 → 100.0`，`score=4 → 7.5`（与示例一致）。
- L5：任意 `score≥600` → `levelProgress=100.0`。
- L0：`levelProgress=0.0`。
- 结果保留 1 位小数。

### AC-6 rule 到达完单率样本规则
- `arrivalSampleCount >= 4`：`arrivalCompletionRate` 为 0~100 的数字百分比，1 位小数；
- `arrivalSampleCount < 4`：`arrivalCompletionRate = null`。

### AC-7 rule 比例字段类型
所有以 `*Rate` 结尾的返回字段类型为 `Number`（`null` 或浮点数），**绝不**是包含 `%` 的字符串。

### AC-8 rule 流水接口分页结构
流水响应包含 `content[] / page / size / total / hasNext`。
- `size < 实际条数` → `hasNext = true`；否则 `false`；
- `content[i]` 字段：`id, occurredAt, type, title, description, changeScore, scoreAfter` 全部存在；
- `page=0&size=20` 默认值生效，超出范围的 `page` 返回空数组且 `total` 仍为真实总数。

### AC-9 rule 规则接口 levels & rules
`/rules` 返回 6 个 levels（L0-L5），scoreRanges 与 levels 的 `minScore/maxScore` 一一对应，不冲突；`rules[]` 至少包含到达完单率、结算速度（3 档）、取消订单、评价 4 个分组，且每个分组的 `items[].changeScore` 与产品指定权重一致。

### AC-10 rule 流水空态 & 持久性
当前阶段无事件写入时，流水接口返回 `content=[]`、`total=0`、`hasNext=false`，不报 500；但 `BossIntegrityFlow` 表与 Repository 必须存在、可 JPA 正常 `save/findAll` 且按分页查询正确（通过单元测试插入 3 条记录后验证分页=正确）。

### AC-11 rule 评价字段占位行为
评价实体未落地前，总览接口始终返回：`positiveReviewRate=null`、`negativeReviewCount=0`、`unreviewedCount=0`，不抛异常。

### AC-12 rule 与现有接口解耦
- 不修改 `StarLevelController` / `UserController.getCredit` / `BossController.getBossStats` 三个方法的签名与返回结构；
- 不新增字段到 `User.creditScore`、不修改 `CreditFlow`、不修改 `UserStarLevel`；
- 诚意分总览 `integrityScore` **不读取** `User.creditScore`（可通过测试断言 + mock 验证）。

### AC-13 rubric 代码分层清晰（0-2，阈值 ≥ 1）
- `2`：Entity/Repository/DTO/Service/Controller 包边界清晰，Controller 仅做参数校验/越权检查/组装 Result，Service 负责聚合与持久化，Repository 只含查询；
- `1`：分层存在但 Service 与 Controller 耦合处有 1 处轻微越界；
- `0`：全部业务塞进 Controller。

### AC-14 rubric 测试覆盖充分性（0-2，阈值 ≥ 1）
- `2`：总览接口（越权/角色/星级边界/progress/样本不足/评价占位）+ 流水分页 + 规则接口 + 两个 Repository 均有单元测试，且全部通过；
- `1`：核心场景（越权/星级边界/流水分页/规则）有测，但边缘 case 缺失 ≤ 3 个；
- `0`：仅 1 个 happy path 测试或无测试。

### AC-15 rule Swagger 注解
3 个接口方法均带 `@Operation(summary + description)`，Controller 类带 `@Tag`，字段含义明确可读。
