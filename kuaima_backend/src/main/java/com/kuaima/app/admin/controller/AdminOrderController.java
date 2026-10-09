package com.kuaima.app.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.common.Result;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.jobcategory.repository.JobCategoryRepository;
import com.kuaima.app.domain.jobcategory.repository.JobIndustryRepository;
import com.kuaima.app.domain.jobcategory.repository.JobEnterpriseTypeRepository;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.domain.review.entity.BossReview;
import com.kuaima.app.domain.review.entity.WorkerReview;
import com.kuaima.app.domain.review.repository.BossReviewRepository;
import com.kuaima.app.domain.review.repository.WorkerReviewRepository;
import com.kuaima.app.security.model.LoginUser;

/**
 * 后台订单管理（基于报名记录的 admin 视角订单列表）
 */
@RestController
@RequestMapping("/admin/orders")
@Tag(name = "后台-用工订单", description = "用工订单管理")
public class AdminOrderController {

    private final BaseOrderItemRespository itemRepository;
    private final BossOrderRespository orderRepository;
    private final UserRepository userRepository;
    private final SettlementRespository settlementRepository;
    private final BossReviewRepository bossReviewRepository;
    private final WorkerReviewRepository workerReviewRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobIndustryRepository jobIndustryRepository;
    private final JobEnterpriseTypeRepository jobEnterpriseTypeRepository;

    public AdminOrderController(BaseOrderItemRespository itemRepository,
                                BossOrderRespository orderRepository,
                                UserRepository userRepository,
                                SettlementRespository settlementRepository,
                                BossReviewRepository bossReviewRepository,
                                WorkerReviewRepository workerReviewRepository,
                                JobCategoryRepository jobCategoryRepository,
                                JobIndustryRepository jobIndustryRepository,
                                JobEnterpriseTypeRepository jobEnterpriseTypeRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.settlementRepository = settlementRepository;
        this.bossReviewRepository = bossReviewRepository;
        this.workerReviewRepository = workerReviewRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.jobIndustryRepository = jobIndustryRepository;
        this.jobEnterpriseTypeRepository = jobEnterpriseTypeRepository;
    }

    /** 订单/报名列表（admin 全量视图，关联雇主名称、零工名称、工种、金额、时间） */
    @Operation(summary = "订单零工明细列表", description = "以一条招工订单和一条零工报名记录的组合为一行，支持状态筛选和分页；父订单号用于展示，报名记录 ID 用于操作")
    @GetMapping
    public Result<Page<JSONObject>> list(@RequestParam(required = false) String status,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String enterpriseTypeId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        java.util.Date startTime = startDate == null ? null
                : java.util.Date.from(startDate.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());
        java.util.Date endTime = endDate == null ? null
                : java.util.Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());
        Page<BaseOrderItem> items = itemRepository.searchAdminOrders(
                status == null || status.isBlank() ? null : status,
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                startTime, endTime, enterpriseTypeId, pageable);

        // 批量查询关联的 BossOrder（获取雇主、工种、金额、时间）
        Set<Long> orderIds = items.stream().map(BaseOrderItem::getOrderId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Map<Long, BossOrder> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            orderRepository.findAllById(orderIds).forEach(o -> orderMap.put(o.getId(), o));
        }

        // 批量查询关联的 User（零工名称、雇主名称）
        Set<Long> userIds = items.stream().map(BaseOrderItem::getUserId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Set<Long> employerIds = orderMap.values().stream().map(BossOrder::getCreateBy).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        userIds.addAll(employerIds);
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        Set<Long> categoryIds = orderMap.values().stream().map(BossOrder::getJobCategoryId)
                .filter(id -> id != null && id > 0).collect(Collectors.toSet());
        orderMap.values().stream().flatMap(o -> parseIds(o.getJobIds()).stream()).forEach(categoryIds::add);
        Map<Long, String> categoryNames = new HashMap<>();
        jobCategoryRepository.findAllById(categoryIds).forEach(c -> categoryNames.put(c.getId(), c.getName()));
        Set<Long> industryIds = orderMap.values().stream().map(BossOrder::getIndustryId)
                .filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Map<Long, String> industryNames = new HashMap<>();
        jobIndustryRepository.findAllById(industryIds).forEach(i -> industryNames.put(i.getId(), i.getName()));
        Map<Long, String> enterpriseTypeNames = new HashMap<>();
        Set<Long> enterpriseTypeIds = orderMap.values().stream()
                .flatMap(o -> parseIds(o.getEnterpriseTypeIds()).stream()).collect(Collectors.toSet());
        if (!enterpriseTypeIds.isEmpty()) {
            jobEnterpriseTypeRepository.findAllById(enterpriseTypeIds)
                    .forEach(e -> enterpriseTypeNames.put(e.getId(), e.getName()));
        }

        Map<Long, Settlement> settlementMap = new HashMap<>();
        Map<Long, BossReview> bossReviewMap = new HashMap<>();
        Map<Long, WorkerReview> workerReviewMap = new HashMap<>();
        if (!items.isEmpty()) {
            List<Long> itemIds = items.stream().map(BaseOrderItem::getId).toList();
            settlementRepository.findByItemIdInOrderByIdDesc(itemIds)
                    .forEach(s -> settlementMap.putIfAbsent(s.getItemId(), s));
            bossReviewRepository.findByItemIdIn(itemIds).forEach(r -> bossReviewMap.put(r.getItemId(), r));
            workerReviewRepository.findByItemIdIn(itemIds).forEach(r -> workerReviewMap.put(r.getItemId(), r));
        }

        Page<JSONObject> views = items.map(item -> {
            JSONObject obj = (JSONObject) JSON.toJSON(item);

            // 关联招工订单信息
            BossOrder order = orderMap.get(item.getOrderId());
            if (order != null) {
                // 列表按“招工订单 + 零工报名记录”展示：父订单号用于展示，报名记录 ID 用于操作。
                obj.put("parentOrderId", order.getId());
                obj.put("orderNumber", order.getOrderNo());
                obj.put("jobTitle", order.getOrderTitle());
                obj.put("jobType", order.getType());
                String selectedJobs = parseIds(order.getJobIds()).stream().map(categoryNames::get)
                        .filter(java.util.Objects::nonNull).collect(Collectors.joining("、"));
                obj.put("jobCategoryName", selectedJobs.isBlank() ? categoryNames.get(order.getJobCategoryId()) : selectedJobs);
                obj.put("industryName", industryNames.get(order.getIndustryId()));
                obj.put("enterpriseTypeName", parseIds(order.getEnterpriseTypeIds()).stream()
                        .map(enterpriseTypeNames::get).filter(java.util.Objects::nonNull).collect(Collectors.joining("、")));
                obj.put("postion", order.getPostion());
                obj.put("amount", order.getSalary());
                obj.put("startTime", order.getStartTime());
                obj.put("endTime", order.getEndTime());
                obj.put("orderNum", order.getOrderNum());

                // 雇主名称
                User employer = userMap.get(order.getCreateBy());
                if (employer != null) {
                    obj.put("employerName",
                            (employer.getCompanyName() != null && !employer.getCompanyName().isBlank())
                                    ? employer.getCompanyName()
                                    : (employer.getNickname() != null ? employer.getNickname() : employer.getUsername()));
                } else {
                    obj.put("employerName", "未知雇主");
                }
            }

            // 零工名称
            User worker = userMap.get(item.getUserId());
            if (worker != null) {
                String workerName = worker.getRealName();
                if (workerName == null || workerName.isBlank()) workerName = worker.getNickname();
                if (workerName == null || workerName.isBlank()) workerName = "零工" + worker.getId();
                obj.put("workerName", workerName);
                obj.put("workerPhone", worker.getPhone());
            } else {
            obj.put("workerName", "未知零工");
            }

            obj.put("applyAt", item.getApplyAt());
            obj.put("hireAt", item.getHireAt());
            obj.put("workAt", item.getWorkAt());
            obj.put("finishAt", item.getFinishAt());
            obj.put("finishDate", item.getFinishDate());

            // 时间线节点：报名/录用/到岗/完工来自报名记录，结算/完成来自实际支付记录。
            // 这些字段不能用订单计划开始/结束时间替代，否则会把计划时间误显示为操作时间。
            Settlement settlement = settlementMap.get(item.getId());
            obj.put("settlementCreatedAt", settlement == null ? null : settlement.getTimestamp());
            obj.put("settlementPayTime", settlement == null ? null : settlement.getPayTime());

            BossReview bossReview = bossReviewMap.get(item.getId());
            if (bossReview != null) {
                Map<String, Object> review = new HashMap<>();
                review.put("score", average(bossReview.getAttitudeScore(), bossReview.getSettlementScore(), bossReview.getEnvironmentScore()));
                review.put("time", bossReview.getCreatedAt());
                review.put("content", bossReview.getContent());
                review.put("dimensions", List.of(
                        Map.of("label", "工作态度", "score", bossReview.getAttitudeScore()),
                        Map.of("label", "工资结算", "score", bossReview.getSettlementScore()),
                        Map.of("label", "工作环境", "score", bossReview.getEnvironmentScore())));
                obj.put("bossReview", review);
            }
            WorkerReview workerReview = workerReviewMap.get(item.getId());
            if (workerReview != null) {
                Map<String, Object> review = new HashMap<>();
                review.put("score", workerReview.getOverallScore());
                review.put("time", workerReview.getCreatedAt());
                review.put("content", workerReview.getContent());
                review.put("dimensions", List.of(
                        Map.of("label", "工作态度", "score", workerReview.getAttitudeScore()),
                        Map.of("label", "工作效率", "score", workerReview.getEfficiencyScore()),
                        Map.of("label", "专业技能", "score", workerReview.getSkillScore())));
                obj.put("workerReview", review);
            }

            return obj;
        });

        return Result.success(views, page, items.getTotalElements());
    }

    @GetMapping("/stats")
    @Operation(summary = "后台订单统计", description = "按完整筛选结果统计订单总数和各状态数量，不受列表分页影响")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String enterpriseTypeId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        java.util.Date startTime = startDate == null ? null
                : java.util.Date.from(startDate.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());
        java.util.Date endTime = endDate == null ? null
                : java.util.Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());
        String normalizedStatus = status == null || status.isBlank() ? null : status;
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        long total = 0;
        for (Object[] row : itemRepository.countAdminOrderStatuses(normalizedStatus, startTime, endTime, enterpriseTypeId)) {
            String rowStatus = row[0] == null ? "未知" : String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            byStatus.put(rowStatus, count);
            total += count;
        }
        result.put("total", total);
        result.put("byStatus", byStatus);
        return Result.success(result);
    }

    private List<Long> parseIds(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.replace("[", "").replace("]", "").replace("\"", "").split(","))
                .map(String::trim).filter(s -> !s.isBlank()).map(Long::valueOf).toList();
    }

    private double average(Integer... values) {
        return java.util.Arrays.stream(values).filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue).average().orElse(0);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "后台取消订单", description = "取消指定报名记录，并记录取消原因")
    public Result<JSONObject> cancel(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String, Object> body,
                                     Authentication authentication) {
        requireAdmin(authentication);
        BaseOrderItem item = itemRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("订单不存在: " + id));
        if ("已完成".equals(item.getStatus()) || "已结算".equals(item.getStatus())) {
            throw new IllegalArgumentException("已完成或已结算订单不可取消");
        }
        item.setStatus("取消报名");
        item.setCancelReason(body == null || body.get("reason") == null ? "管理员取消" : String.valueOf(body.get("reason")));
        item.setCancelDate(new java.sql.Date(System.currentTimeMillis()));
        BaseOrderItem saved = itemRepository.save(item);
        return Result.success((JSONObject) JSON.toJSON(saved));
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)
                || user.role() == null || !user.role().startsWith("ADMIN_")) {
            throw new ForbiddenBusinessException("仅管理员可操作");
        }
    }
}
