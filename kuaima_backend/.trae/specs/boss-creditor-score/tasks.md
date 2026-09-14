# 老板诚意分模块实现任务清单（tasks）

> 依赖关系：T1 → T2 → T3 → T4 → T5（T6 可并行）。T7 在代码完成后执行。

## Task 1: 领域模型与数据库迁移（Entity + Repository + DDL）

**目标**：建立老板诚意分快照表 `boss_integrity_score`、诚意分流水表 `boss_integrity_flow`，以及对应 JPA 实体与 Repository。

**关联 AC**：AC-3, AC-8, AC-10, AC-12

### 交付物
1. `BossIntegrityScore.java`（`domain/bossintegrity/entity/`）
   - 字段：`id, userId(Long, unique), score(Integer), starLevel(Integer), periodStart(LocalDate), periodEnd(LocalDate), lastCalcAt(LocalDateTime)`；继承 `BaseEntity`。
2. `BossIntegrityFlow.java`（`domain/bossintegrity/entity/`）
   - 字段：`id, userId(Long, index), occurredAt(LocalDateTime, index), type(String, 60), title(String, 100), description(String, 500), changeScore(Integer), scoreAfter(Integer), bizId(Long, 关联订单/结算单ID，可空)`；继承 `BaseEntity`。
3. `BossIntegrityScoreRepository.java`（`domain/bossintegrity/repository/`）
   - `Optional<BossIntegrityScore> findByUserId(Long userId)`；
   - 继承 `JpaRepository<BossIntegrityScore, Long>`。
4. `BossIntegrityFlowRepository.java`（`domain/bossintegrity/repository/`）
   - `Page<BossIntegrityFlow> findByUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable)`；
   - 继承 `JpaRepository<BossIntegrityFlow, Long>`。
5. 迁移 SQL：`src/main/resources/db/boss_integrity_schema.sql`
   - 两张表的 DDL，含 userId 索引、occurredAt 索引，注释字段与枚举类型。

### 本地测试要求（TR）

| TR | 类型 | 可观察通过条件 |
|---|---|---|
| T1-TR1 | rule | Spring 启动后 `BossIntegrityScoreRepository.findByUserId` 可被 MockMvc 上下文注入，无启动异常。 |
| T1-TR2 | rule | `BossIntegrityFlowRepository.findByUserIdOrderByOccurredAtDesc` 在 H2 内存库中插入 3 条不同 occurredAt 的流水，按 id=2/3/4 分页 `page=0,size=2` 返回最新 2 条且倒序，`total=3, hasNext=true`。 |
| T1-TR3 | rule | `BossIntegrityScore(userId, score, starLevel, periodStart, periodEnd, lastCalcAt)` save 后可 findByUserId 原样取出，字段一致。 |
| T1-TR4 | rule | 生成的 DDL 文件可在 MySQL/H2 兼容模式下无语法错误执行（手工审阅，或通过 `spring.jpa.hibernate.ddl-auto=validate` 不报错）。 |

---

## Task 2: DTO（请求/响应结构）

**目标**：为 3 个接口定义强类型响应 DTO，不再返回 `Map`，保证字段名与 spec 完全一致。

**关联 AC**：AC-3, AC-7, AC-8, AC-9

### 交付物（放在 `domain/bossintegrity/model/` 或 `controller/boss/dto/`，择一统一）
1. `BossCreditorScoreOverviewDto.java`
   - 字段：`userId, starLevel, starName, starDescription, integrityScore, periodStart(String yyyy-MM-dd), periodEnd(String yyyy-MM-dd), scoreRanges(List<Integer>=5), levelProgress(Double), benefitCount(Integer), benefits(List<String>), arrivalCompletionRate(Double), arrivalSampleCount(Integer), settlementWithin1hRate(Double), settlementWithin24hRate(Double), positiveReviewRate(Double), negativeReviewCount(Integer), unreviewedCount(Integer), completedOrderCount(Integer), cancelledOrderCount(Integer)`。
2. `BossIntegrityFlowItemDto.java`
   - `id, occurredAt(LocalDateTime ISO-8601), type, title, description, changeScore, scoreAfter`。
3. `BossIntegrityFlowPageDto.java`
   - `content(List<BossIntegrityFlowItemDto>), page, size, total, hasNext`。
4. `BossIntegrityRulesDto`（内部可嵌套 LevelsDto + RulesDto）
   - Levels：`level, name, minScore, maxScore, benefits(List<String>)`
   - Rules：`code, title, description, items(List<RuleItemDto>)`，RuleItem：`condition, changeScore, label`

### 本地测试要求（TR）

| TR | 类型 | 可观察通过条件 |
|---|---|---|
| T2-TR1 | rule | 使用 fastjson2 将 `BossCreditorScoreOverviewDto` 序列化为 JSON，字段名与 AC-3 列表逐字段比对无缺键。 |
| T2-TR2 | rule | `*Rate` 字段均为 `Double/Number` 型，toString 不含 `%` 字符。 |
| T2-TR3 | rule | `BossIntegrityRulesDto` levels 长度 6，rules 长度 ≥ 4。 |

---

## Task 3: 枚举与 Service（聚合计算 + 缓存 + 流水持久化）

**目标**：实现诚意分核心业务逻辑，隔离到独立 Service 层，不污染 Controller。

**关联 AC**：AC-4, AC-5, AC-6, AC-11, AC-12, AC-13

### 交付物
1. `BossIntegrityFlowType.java` 枚举（`domain/bossintegrity/constant/` 或与 Service 同包）
   - 8 个值：ARRIVAL_COMPLETED / ARRIVAL_NOT_COMPLETED / SETTLE_WITHIN_1H / SETTLE_WITHIN_24H / SETTLE_OVER_24H / POSITIVE_REVIEW / NEGATIVE_REVIEW / ORDER_CANCELLED；含中文 title。
2. `BossIntegrityLevelPolicy.java`（`domain/bossintegrity/constant/`）
   - `scoreRanges = [0, 40, 100, 200, 600]` 常量。
   - 方法 `starName(level)`、`starDescription(level)`、`benefits(level)` 返回对应文本列表。
   - 方法 `resolveStarLevel(int score)`、`calcLevelProgress(int score, int level)`。
3. `BossIntegrityScoreService.java`（`domain/bossintegrity/service/`）
   - 构造注入：`BossIntegrityScoreRepository`, `BossIntegrityFlowRepository`, `BossOrderRespository`, `BaseOrderItemRespository`, `SettlementRespository`, `UserRepository`。
   - 常量 `PERIOD_DAYS = 90`。
   - **公共方法 1**：`BossCreditorScoreOverviewDto computeOverview(Long bossId)`
     - 校验 user.role == BOSS（Service 内做？或 Controller 做，保持单一职责；建议 Controller 做越权+角色，Service 只接收 BOSS 的 userId）。
     - 计算 `periodStart / periodEnd`（LocalDate.now(ZoneId.of("Asia/Shanghai"))）。
     - 周期内聚合：
       - **到达完单率**：查询老板名下所有 `BaseOrderItem` 中 `hireDate 在周期内` 的样本；分母 = 样本量（`arrivalSampleCount`），分子 = `finishDate != null 且 cancelDate == null` 数量。若 `arrivalSampleCount < 4` → `arrivalCompletionRate = null`。
       - **结算速度**：查询周期内 `已支付` 的结算单 `Settlement`，对每条计算 `DURATION(payTime - orderItem.finishDate 或 order.endTime)`；统计落入 `<1h`、`<24h`、`>24h` 的比例。无结算单则二者都为 null。
       - **订单计数**：`completedOrderCount = 周期内 BossOrder.orderStatus ∈ {已完成, 待结算}`；`cancelledOrderCount = orderStatus = 取消招工 或 报名 cancelDate 非空`。
       - **诚意分积分**：聚合上述事件类型 × 权重（同 AC-9 的 rules.items.changeScore）。
       - `positiveReviewRate = null, negativeReviewCount = 0, unreviewedCount = 0`（评价未落地占位）。
     - 调用 `resolveStarLevel / calcLevelProgress` 映射星级。
     - 从 `BossIntegrityLevelPolicy.benefits(level)` 取 benefits。
     - **持久化快照**：`BossIntegrityScore` 存在则更新，不存在则新建；若 `lastCalcAt.toLocalDate() == today` 则只更新 score 以外的字段，不重复算分（幂等）。
   - **公共方法 2**：`BossIntegrityFlowPageDto queryFlows(Long bossId, int page, int size)`
     - 分页查询 Repository；结果 DTO 映射；`hasNext = (page+1)*size < total`。
   - **公共方法 3**：`BossIntegrityRulesDto getRules()`
     - 纯静态返回（AC-9）。

### 本地测试要求（TR）

| TR | 类型 | 可观察通过条件 |
|---|---|---|
| T3-TR1 | rule（AC-4 星级边界） | 构造 score=0/39/40/99/100/199/200/599/600，`resolveStarLevel` 返回对应 starLevel。 |
| T3-TR2 | rule（AC-5 progress） | score=4 L1 → 7.5；score=1 L1 → 0.0；score=40 L1 → 100.0；score=600 L5 → 100.0；score=0 L0 → 0.0。 |
| T3-TR3 | rule（AC-6 样本阈值） | arrivalSampleCount=3 → arrivalCompletionRate=null；=4 → 数字（即使 0.0 也不是 null）。 |
| T3-TR4 | rule（AC-11 评价占位） | Service 返回 DTO 中 `positiveReviewRate==null && negativeReviewCount==0 && unreviewedCount==0`。 |
| T3-TR5 | rule（AC-12 解耦） | Mock `UserRepository.findById(bossId)` 返回的 `User.creditScore` 不被读取；通过 mockito `verify(userRepository, never()).findById` 取 creditScore 验证。 |
| T3-TR6 | rubric（AC-13 分层） | Service 内无 `HttpServletRequest/Response/Authentication` 相关 import，只做纯业务；越权在 Controller。 |

---

## Task 4: Controller（路由 + Swagger + 越权/角色校验）

**目标**：新增 `CreditorScoreController`（或放在 `BossController` 中，但为避免耦合建议新文件），暴露 3 个接口。

**关联 AC**：AC-1, AC-2, AC-15

### 交付物
1. `BossCreditorScoreController.java`（`controller/boss/`）
   - `@Tag(name = "老板诚意分", description = "老板端诚意分/雇主星级、明细流水、规则权益接口")`
   - `@RequestMapping("/boss/creditor-score")`
   - **接口 1**：`@GetMapping("/{userId}")` `@Operation(summary="老板诚意分概览", ...)`
     - 入参：`@PathVariable Long userId` + `Authentication authentication`。
     - 校验：`userId != currentUid(authentication)` → 403。
     - 校验：user.role != BOSS → 400。
     - 调用 `service.computeOverview(userId)`，包装 `Result.success` 返回。
   - **接口 2**：`@GetMapping("/{userId}/flows")` `@Operation(summary="诚意分流水（分页）", ...)`
     - 入参：`@PathVariable Long userId` + `@RequestParam(defaultValue="0") int page` + `@RequestParam(defaultValue="20") int size` + `Authentication`。
     - 同样越权 + 角色校验。
     - 调用 `service.queryFlows(userId, page, size)`。
   - **接口 3**：`@GetMapping("/rules")` `@Operation(summary="诚意分星级权益/评分规则（全局）", ...)`
     - 无 userId，无越权（但仍需登录即可，默认 `SecurityConfig` 下 `/boss/**` 需要鉴权即可；若有放行 `/boss/creditor-score/rules` 请求也不过期，不强制改 SecurityConfig，放行不要求）。
     - 调用 `service.getRules()`。

### 本地测试要求（TR）

| TR | 类型 | 可观察通过条件 |
|---|---|---|
| T4-TR1 | rule（AC-1 越权） | Mock `Authentication.uid=1`，请求 `userId=2` → 返回 Result.code=403。 |
| T4-TR2 | rule（AC-2 角色） | Mock user.role=USER → Result.code=400，message 含「诚意分仅支持老板身份」。 |
| T4-TR3 | rule（AC-15 Swagger） | 3 个方法均有 `@Operation`，类有 `@Tag`。代码审阅通过。 |
| T4-TR4 | rule | `/rules` 返回 200，levels 长度 6。 |

---

## Task 5: 单元测试集成（Spring Boot Test）

**目标**：覆盖 AC-1~AC-12 全部 rule 型 AC 与 rubric 可评估。

### 交付物
1. `src/test/java/.../controller/boss/BossCreditorScoreControllerTests.java`
   - 基于 MockMvc 或 Controller 直接调用（参考 AuthControllerWechatLoginTests 的风格：手动注入 mock）。
   - 覆盖：越权、非老板角色、正常总览返回字段齐全、星级边界（mock service 返回指定 score）、流水分页（构造 repo 存 3 条后分页）、规则接口 levels=6。
2. `src/test/java/.../domain/bossintegrity/BossIntegrityPolicyTests.java`
   - 星级边界 9 值断言（AC-4）。
   - levelProgress 5 个边界值（AC-5）。
3. `src/test/java/.../domain/bossintegrity/BossIntegrityServiceTests.java`
   - 到达样本 3 与 4 的 null/非 null（AC-6）。
   - 评价占位（AC-11）。
   - mock UserRepository 验证不读 creditScore（AC-12/T3-TR5）。

### 本地测试要求（TR）

| TR | 类型 | 可观察通过条件 |
|---|---|---|
| T5-TR1 | rule（AC-14 测试通过） | `mvnw test -Dtest=BossCreditorScoreControllerTests,BossIntegrityPolicyTests,BossIntegrityServiceTests,AuthControllerWechatLoginTests -Dspring.profiles.active=test` 全部通过（BUILD SUCCESS）。 |
| T5-TR2 | rubric（AC-14 覆盖） | 上述测试覆盖：越权/角色/星级边界×9/progress×5/到达样本/评价占位/流水分页/规则接口。至少命中 13 个不同 case，得 2 分；≤10 得 1 分；<7 得 0。 |

---

## Task 6: SecurityConfig 确认（不改白名单）

**目标**：确保 `/boss/creditor-score/**` 三条路由都在现有 JWT 过滤器保护下（不是 permissive）。

**关联 AC**：AC-1

### 交付物
1. 审阅 `SecurityConfig.java` 的 `requestMatchers` 白名单，确认无提前放行 `/boss/creditor-score/**` 的路径。
2. 如果 `/boss/**` 没有特殊放行，则无需改代码；只需在任务证据中记录审阅结论。

### TR
| TR | 类型 | 可观察通过条件 |
|---|---|---|
| T6-TR1 | rule | 白名单不含 `/boss/creditor-score`；SecurityConfig 代码审阅确认。 |

---

## Task 7: 关系边界文档（在 spec 已写，这里作为审阅任务）

**目标**：明确与三个现有接口的关系，不发生行为破坏。

### 交付物（无需新文件，在 Review 证据中写明结论即可）
1. `GET /star-level/{userId}` — 旧页面不使用新 DTO/Repo；新老板端页面也不调用它。无修改。
2. `GET /user/{id}/credit` — 工人信用分。不读 `CreditFlow / creditScore`。无修改。
3. `GET /boss/stats` — 首页统计。诚意分聚合计算可能复用其 Repository 查询方法（findByCreateBy 等），但 Service 独立，互不影响返回结构。无修改。

### TR
| TR | 类型 | 可观察通过条件 |
|---|---|---|
| T7-TR1 | rule（AC-12） | `git diff --name-only` 不含 `StarLevelController.java`、`UserController.java`、`BossController.java`、`CreditFlow.java`、`UserStarLevel.java`。 |
