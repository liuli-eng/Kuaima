package com.kuaima.app.controller.boss;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.model.BossOrderQuery;
import com.kuaima.app.domain.boss.service.BossOrderService;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.HotItem;
import com.kuaima.app.domain.jobcategory.service.JobCategoryService;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.service.CertificationService;
import com.kuaima.app.security.model.LoginUser;

@RestController
@RequestMapping("/boss")
@Tag(name = "老板-招工", description = "老板发布招工、订单管理、报名处理")
public class BossController {

    private final BossOrderService bossOrderService;
    private final JobCategoryService jobCategoryService;
    private final CertificationService certificationService;

    public BossController(BossOrderService bossOrderService, JobCategoryService jobCategoryService,
                          CertificationService certificationService) {
        this.bossOrderService = bossOrderService;
        this.jobCategoryService = jobCategoryService;
        this.certificationService = certificationService;
    }

    // ==================== 招工订单 ====================

    /** 发布订单 */
    @Operation(summary = "发布招工订单", description = "创建 BossOrder，初始状态自动为「待审核」，admin 审核通过后变为「招工中」并广播新岗位消息。必填：orderTitle、type、postion、orderNum、duration、salary")
    @PostMapping("/order")
    public Result<BossOrder> createOrder(@RequestBody BossOrder order, Authentication authentication) {
        Long bossId = requireCurrentBossId(authentication);
        certificationService.requirePublishEligibility(bossId);
        order.setCreateBy(bossId);
        return Result.success(bossOrderService.createOrder(order));
    }

    @Operation(summary = "发布资格查询", description = "查询当前老板个人实名认证和企业认证状态")
    @GetMapping("/publish-eligibility")
    public Result<Map<String, Object>> publishEligibility(Authentication authentication) {
        return Result.success(certificationService.publishEligibility(requireCurrentBossId(authentication)));
    }

    /** 修改订单（仅招工中） */
    @Operation(summary = "修改招工订单", description = "仅「待审核」或「招工中」状态可修改；请求体字段非空才会被更新。类型改为非月结时清空 trialDuration；试工时间仅在类型为 month 时可设置")
    @PutMapping("/order/{id}")
    public Result<BossOrder> updateOrder(@PathVariable Long id, @RequestBody BossOrder order) {
        return Result.success(bossOrderService.updateOrder(id, order));
    }

    /** 订单详情 */
    @Operation(summary = "订单详情", description = "返回完整订单对象（含 id、date、createBy、timestamp 及订单字段）")
    @GetMapping("/order/{id}")
    public Result<BossOrder> getOrder(@PathVariable Long id) {
        return Result.success(bossOrderService.getOrder(id));
    }

    /**
     * 订单列表分页查询：/boss/order?type=daily&status=招工中&title=xxx&page=0&size=10
     * type 取值：daily(每天日结) / heldBack(压薪日结) / month(月结)
     * page 从 0 开始，size 默认 10
     */
    @Operation(summary = "订单列表分页", description = "仅返回当前老板账号创建的订单。所有非空筛选条件按 AND 组合，支持 type、status、title、startDate/endDate、jobCategoryId、salaryMin/salaryMax、经纬度距离、experience、gender、tags、tagMode(ALL/ANY) 和分页；返回准确 total 及 currentApply")
    @GetMapping("/order")
    public Result<List<BossOrder>> listOrders(@ModelAttribute BossOrderQuery query,
                                               Authentication authentication) {
        Long bossId = requireCurrentBossId(authentication);
        Page<BossOrder> result = bossOrderService.listOrders(bossId, query);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 删除订单（仅招工中/已取消） */
    @Operation(summary = "删除订单", description = "仅「待审核」/「审核拒绝」/「招工中」/「取消招工」状态的订单可删除")
    @DeleteMapping("/order/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        bossOrderService.deleteOrder(id);
        return Result.success();
    }

    /** 订单状态流转：/boss/order/{id}/status?target=招工结束|待结算|已完成|取消招工 */
    @Operation(summary = "订单状态流转", description = "target 取值：招工结束 / 待结算 / 已完成 / 取消招工。取消招工会将该订单下所有未完成报名记录置为「取消招工」并记录取消时间")
    @PutMapping("/order/{id}/status")
    public Result<BossOrder> changeOrderStatus(@PathVariable Long id, @RequestParam String target) {
        return Result.success(bossOrderService.changeOrderStatus(id, target));
    }

    /**
     * 【预留】岗位快开始提醒：手动触发，通知该订单 已报名/已录用/已到岗 的用户
     * （当前不做定时任务，由前端/人工调用；后续如需自动提醒可加定时扫描）
     */
    @Operation(summary = "岗位快开始提醒", description = "手动触发「岗位快开始」通知，推送给该订单下「已报名/已录用/已到岗」的零工。当前不做定时任务，由前端/人工调用")
    @PostMapping("/order/{id}/remind-start")
    public Result<Void> notifyOrderStart(@PathVariable Long id) {
        bossOrderService.notifyOrderStart(id);
        return Result.success();
    }

    // ==================== 报名记录 ====================

    /** 用户报名：/boss/order/{orderId}/apply?userId=1&remark=xxx&trial=true（trial 仅月结订单有效） */
    @Operation(summary = "用户报名岗位", description = "订单须为「招工中」，同一用户不可重复报名；「已录用+已到岗+已完成」人数达到招工人数后不可再报名。trial「我要试工」标记仅月结(month)订单可传 true")
    @PostMapping("/order/{orderId}/apply")
    public Result<BaseOrderItem> applyOrder(@PathVariable Long orderId,
                                            @RequestParam Long userId,
                                            @RequestParam(required = false) String remark,
                                            @RequestParam(required = false) Boolean trial) {
        return Result.success(bossOrderService.applyOrder(orderId, userId, remark, trial));
    }

    /** 老板录用 */
    @Operation(summary = "老板录用零工", description = "仅「已报名」的记录可录用，状态变为「已录用」并记录录用时间")
    @PutMapping("/item/{id}/hire")
    public Result<BaseOrderItem> hireItem(@PathVariable Long id) {
        return Result.success(bossOrderService.hireItem(id));
    }

    /** 老板拒绝待审核报名。 */
    @Operation(summary = "老板拒绝报名", description = "仅岗位所属老板可拒绝状态为「已报名」的记录，并通知零工")
    @PutMapping("/item/{id}/reject")
    public Result<BaseOrderItem> rejectItem(@PathVariable Long id,
                                             @RequestParam(required = false) String reason,
                                             Authentication authentication) {
        return Result.success(bossOrderService.rejectItem(id, requireCurrentBossId(authentication), reason));
    }

    /** 用户确认到岗 */
    @Operation(summary = "用户确认到岗", description = "仅「已录用」的记录可确认到岗，状态变为「已到岗」并记录到岗时间")
    @PutMapping("/item/{id}/work")
    public Result<BaseOrderItem> confirmWork(@PathVariable Long id) {
        return Result.success(bossOrderService.confirmWork(id));
    }

    /** 完成 */
    @Operation(summary = "完成报名记录", description = "仅「已到岗」的记录可完成，状态变为「已完成」并记录完成时间")
    @PutMapping("/item/{id}/finish")
    public Result<BaseOrderItem> finishItem(@PathVariable Long id) {
        return Result.success(bossOrderService.finishItem(id));
    }

    /** 取消报名：/boss/item/{id}/cancel?reason=xxx */
    @Operation(summary = "取消报名", description = "仅「已报名」/「已录用」状态可取消，状态变为「取消报名」并记录取消时间、原因")
    @PutMapping("/item/{id}/cancel")
    public Result<BaseOrderItem> cancelItem(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return Result.success(bossOrderService.cancelItem(id, reason));
    }

    /** 某订单的报名列表 */
    @Operation(summary = "订单报名列表", description = "返回该订单下所有报名记录数组，包含 userId 对应的 nickname（nickname 为非持久化展示字段）")
    @GetMapping("/order/{orderId}/items")
    public Result<List<BaseOrderItem>> listItemsByOrder(@PathVariable Long orderId) {
        return Result.success(bossOrderService.listItemsByOrder(orderId));
    }

    /** 某用户的报名记录：/boss/user/items?userId=1 */
    @Operation(summary = "用户报名记录", description = "返回该用户所有报名记录数组")
    @GetMapping("/user/items")
    public Result<List<BaseOrderItem>> listItemsByUser(@RequestParam Long userId) {
        return Result.success(bossOrderService.listItemsByUser(userId));
    }

    // ==================== 草稿管理 ====================

    /** 保存草稿 */
    @Operation(summary = "保存订单草稿", description = "创建 BossOrder，orderStatus 置为「草稿」")
    @PostMapping("/order/draft")
    public Result<BossOrder> saveDraft(@RequestBody BossOrder order) {
        return Result.success(bossOrderService.saveDraft(order));
    }

    /** 草稿列表：/boss/orders/drafts?userId=1 */
    @Operation(summary = "草稿订单列表", description = "返回当前用户的草稿订单列表")
    @GetMapping("/orders/drafts")
    public Result<List<BossOrder>> listDrafts(@RequestParam Long userId) {
        return Result.success(bossOrderService.listDrafts(userId));
    }

    /** 更新草稿 */
    @Operation(summary = "更新订单草稿", description = "按 id 更新草稿订单字段")
    @PutMapping("/order/{id}/draft")
    public Result<BossOrder> updateDraft(@PathVariable Long id, @RequestBody BossOrder order) {
        return Result.success(bossOrderService.updateDraft(id, order));
    }

    // ==================== 统计与资料 ====================

    /** 老板首页统计：/boss/stats?userId=1 */
    @Operation(summary = "老板首页统计", description = "返回 totalOrders、recruitingCount、applicantCount、settledAmount 等汇总数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getBossStats(@RequestParam Long userId) {
        return Result.success(bossOrderService.getBossStats(userId));
    }

    /** 工种分类：/boss/job-categories */
    @Operation(summary = "热门工种列表", description = "兼容原有前端路径，返回基础数据表中启用的热门工种；完整三级分类请使用 /job-categories/tree")
    @GetMapping("/job-categories")
    public Result<List<HotItem>> getJobCategories() {
        return Result.success(jobCategoryService.hot());
    }

    /** 老板资料：/boss/profile/{userId} */
    @Operation(summary = "老板资料详情", description = "返回老板用户完整信息（含 companyName、industry、contact、contactPhone 等企业字段）")
    @GetMapping("/profile/{userId}")
    public Result<User> getBossProfile(@PathVariable Long userId) {
        return Result.success(bossOrderService.getBossProfile(userId));
    }

    /** 老板账户统计：/boss/profile/{userId}/stats */
    @Operation(summary = "老板账户统计", description = "返回 totalOrders、recruitingCount、applicantCount、settledAmount 等账户汇总")
    @GetMapping("/profile/{userId}/stats")
    public Result<Map<String, Object>> getBossProfileStats(@PathVariable Long userId) {
        return Result.success(bossOrderService.getBossProfileStats(userId));
    }

    // ==================== 高级筛选（零工端） ====================

    /** 高级筛选岗位：/boss/order/filter?city=&salaryMin=&salaryMax=&tag=&type=&duration=&page=0&size=20 */
    @Operation(summary = "高级筛选岗位", description = "扩展 BossOrder 查询参数，支持 city、salaryMin/salaryMax 薪资范围、tag 标签、type 招工类型、duration 工作时长筛选")
    @GetMapping("/order/filter")
    public Result<List<BossOrder>> filterOrders(@RequestParam(required = false) String city,
                                                @RequestParam(required = false) Integer salaryMin,
                                                @RequestParam(required = false) Integer salaryMax,
                                                @RequestParam(required = false) String tag,
                                                @RequestParam(required = false) String type,
                                                @RequestParam(required = false) Integer duration,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        Page<BossOrder> result = bossOrderService.filterOrders(city, salaryMin, salaryMax, tag, type, duration, page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 零工的月结订单列表：/boss/order/monthly?userId=1 */
    @Operation(summary = "零工月结订单列表", description = "返回零工的月结类型订单列表（BaseOrderItem 关联 BossOrder where type=month）")
    @GetMapping("/order/monthly")
    public Result<List<BaseOrderItem>> listMonthlyOrders(@RequestParam Long userId) {
        return Result.success(bossOrderService.listMonthlyOrders(userId));
    }

    /** 零工的压薪日结订单列表：/boss/order/press-salary?userId=1 */
    @Operation(summary = "零工压薪订单列表", description = "返回零工的压薪类型订单列表（type=heldBack）")
    @GetMapping("/order/press-salary")
    public Result<List<BaseOrderItem>> listPressSalaryOrders(@RequestParam Long userId) {
        return Result.success(bossOrderService.listPressSalaryOrders(userId));
    }

    // ==================== 企业认证 ====================

    /** 提交企业认证：POST /boss/enterprise-cert */
    @Operation(summary = "提交企业认证", description = "请求体含 userId、companyName、industry、licenseNo、legalRep。设置 User.certType=ENTERPRISE、certStatus=待审核")
    @PostMapping("/enterprise-cert")
    public Result<User> submitEnterpriseCert(@RequestBody Map<String, String> body, Authentication authentication) {
        Long userId = requireCurrentBossId(authentication);
        return Result.success(certificationService.submitEnterprise(userId,
                body.get("companyName"), body.get("industry"),
                body.get("licenseNo"), body.get("legalRep")));
    }

    /** 从 JWT 认证主体提取老板 ID。 */
    private Long currentBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser
                && UserRole.BOSS.equals(loginUser.role())) {
            if (loginUser.id() == null) {
                throw new IllegalStateException("老板登录身份缺少用户ID，请重新登录");
            }
            return loginUser.id();
        }
        throw new ForbiddenBusinessException("当前登录账号不是老板账号");
    }

    private Long requireCurrentBossId(Authentication authentication) {
        Long bossId = currentBossId(authentication);
        if (bossId == null) {
            throw new IllegalStateException("当前登录账号不是老板账号");
        }
        return bossId;
    }
}
