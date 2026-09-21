package com.kuaima.app.domain.coupon.service;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.coupon.entity.Coupon;
import com.kuaima.app.domain.coupon.entity.UserCoupon;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BossCouponService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<String> STATUSES = Set.of("UNUSED", "USED", "EXPIRED");
    private final UserCouponRepository userCoupons;
    private final CouponRepository coupons;
    private final BossOrderRespository orders;
    private final BaseOrderItemRespository items;
    private final SettlementRespository settlements;
    @Value("${kuaima.settle.service-fee-rate:0}")
    private int serviceFeeRate;

    public BossCouponService(UserCouponRepository userCoupons, CouponRepository coupons,
                             BossOrderRespository orders, BaseOrderItemRespository items,
                             SettlementRespository settlements) {
        this.userCoupons = userCoupons;
        this.coupons = coupons;
        this.orders = orders;
        this.items = items;
        this.settlements = settlements;
    }

    @Transactional
    public List<Map<String, Object>> list(Long bossId, String status) {
        String normalized = normalizeStatus(status);
        List<UserCoupon> records = userCoupons.findByUserId(bossId);
        LocalDate today = LocalDate.now(ZONE);
        boolean changed = false;
        for (UserCoupon record : records) {
            if (isExpired(record, today) && !"EXPIRED".equals(record.getStatus())) {
                record.setStatus("EXPIRED");
                changed = true;
            }
        }
        if (changed) userCoupons.saveAll(records);
        Map<Long, Coupon> definitions = new HashMap<>();
        coupons.findAllById(records.stream().map(UserCoupon::getCouponId).filter(Objects::nonNull).distinct().toList())
                .forEach(coupon -> definitions.put(coupon.getId(), coupon));
        return records.stream().filter(record -> normalized == null || normalized.equals(record.getStatus()))
                .map(record -> view(record, definitions.get(record.getCouponId()))).toList();
    }

    @Transactional
    public void redeem(Long bossId, Long userCouponId, BossOrder order) {
        if (userCouponId == null) return;
        UserCoupon record = userCoupons.findByIdForUpdate(userCouponId)
                .orElseThrow(() -> new EntityNotFoundException("优惠券领取记录不存在: " + userCouponId));
        if (!Objects.equals(record.getUserId(), bossId)) throw new ForbiddenBusinessException("无权使用该优惠券");
        if (isExpired(record, LocalDate.now(ZONE))) {
            record.setStatus("EXPIRED");
            userCoupons.save(record);
            throw new IllegalArgumentException("优惠券已过期");
        }
        if (!"UNUSED".equals(record.getStatus())) throw new IllegalArgumentException("优惠券已使用或不可用");
        Coupon coupon = coupons.findByIdForUpdate(record.getCouponId())
                .orElseThrow(() -> new EntityNotFoundException("优惠券不存在: " + record.getCouponId()));
        BigDecimal threshold = coupon.getThreshold() != null ? coupon.getThreshold()
                : Optional.ofNullable(coupon.getMinSpend()).orElse(BigDecimal.ZERO);
        BigDecimal orderAmount = order.getSalary()
                .multiply(BigDecimal.valueOf(order.getDuration()))
                .multiply(BigDecimal.valueOf(order.getOrderNum()));
        if (orderAmount.compareTo(threshold) < 0) {
            throw new IllegalArgumentException("订单金额未达到优惠券使用门槛，需满" + threshold.stripTrailingZeros().toPlainString() + "元");
        }
        record.setStatus("USED");
        record.setUsedAt(Timestamp.valueOf(LocalDateTime.now(ZONE)));
        record.setUseOrderId(order.getId());
        userCoupons.save(record);
        coupon.setUsed((coupon.getUsed() == null ? 0 : coupon.getUsed()) + 1);
        coupons.save(coupon);
    }

    /**
     * 查询当前老板可选择核销优惠券的待结算订单。订单归属、优惠券归属均由服务端校验。
     * 金额以元返回，与优惠券 amount/threshold 的存储口径一致。
     */
    @Transactional(readOnly = true)
    public Map<String, Object> availableOrders(Long bossId, Long userCouponId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 必须大于等于0，size 范围为1-100");
        }
        if (orders == null || items == null || settlements == null) {
            throw new IllegalStateException("优惠券可用订单查询未配置订单服务");
        }
        UserCoupon record = userCoupons.findById(userCouponId)
                .orElseThrow(() -> new EntityNotFoundException("优惠券领取记录不存在: " + userCouponId));
        if (!Objects.equals(record.getUserId(), bossId)) {
            throw new ForbiddenBusinessException("无权查询该优惠券可用订单");
        }
        if (isExpired(record, LocalDate.now(ZONE))) {
            throw new IllegalArgumentException("优惠券已过期");
        }
        if (!"UNUSED".equals(record.getStatus())) {
            throw new IllegalArgumentException("优惠券已使用或不可用");
        }
        Coupon coupon = coupons.findById(record.getCouponId())
                .orElseThrow(() -> new EntityNotFoundException("优惠券不存在: " + record.getCouponId()));

        List<BossOrder> ownedOrders = orders.findByCreateByOrderByIdDesc(bossId);
        List<Long> orderIds = ownedOrders.stream().map(BossOrder::getId).filter(Objects::nonNull).toList();
        List<BaseOrderItem> allItems = orderIds.isEmpty() ? List.of() : items.findByOrderIdIn(orderIds);
        List<Long> itemIds = allItems.stream().map(BaseOrderItem::getId).filter(Objects::nonNull).toList();
        Map<Long, List<Settlement>> settlementByItem = itemIds.isEmpty() ? Map.of()
                : settlements.findByItemIdInOrderByIdDesc(itemIds).stream()
                        .collect(Collectors.groupingBy(Settlement::getItemId));
        Map<Long, List<BaseOrderItem>> itemsByOrder = allItems.stream()
                .collect(Collectors.groupingBy(BaseOrderItem::getOrderId));

        List<Map<String, Object>> records = new ArrayList<>();
        for (BossOrder order : ownedOrders) {
            if (order.getId() == null || !isSettlementOrder(order)) continue;
            List<BaseOrderItem> candidates = itemsByOrder.getOrDefault(order.getId(), List.of()).stream()
                    .filter(item -> isPendingItem(item, settlementByItem.getOrDefault(item.getId(), List.of())))
                    .toList();
            if (candidates.isEmpty()) continue;
            BigDecimal jobAmount = BigDecimal.ZERO;
            BigDecimal serviceFee = BigDecimal.ZERO;
            for (BaseOrderItem item : candidates) {
                Settlement pending = settlementByItem.getOrDefault(item.getId(), List.of()).stream()
                        .filter(s -> SettlementStatus.PENDING.equals(s.getStatus())).findFirst().orElse(null);
                BigDecimal wage = pending != null && pending.getWage() != null ? pending.getWage() : estimateWage(order, item);
                BigDecimal fee = pending != null && pending.getServiceFee() != null ? pending.getServiceFee() : calculateServiceFee(wage);
                jobAmount = jobAmount.add(wage);
                serviceFee = serviceFee.add(fee);
            }
            BigDecimal threshold = coupon.getThreshold() != null ? coupon.getThreshold()
                    : Optional.ofNullable(coupon.getMinSpend()).orElse(BigDecimal.ZERO);
            boolean usable = jobAmount.compareTo(threshold) >= 0;
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", order.getId());
            view.put("title", StringUtils.hasText(order.getOrderTitle()) ? order.getOrderTitle() : order.getPostion());
            view.put("workerCount", candidates.size());
            view.put("settlementType", order.getType());
            view.put("address", order.getAddress());
            view.put("longitude", order.getLongitude());
            view.put("latitude", order.getLatitude());
            view.put("jobAmount", jobAmount);
            view.put("serviceFee", serviceFee);
            view.put("usable", usable);
            view.put("unusableReason", usable ? "" : "订单岗位费用未达到优惠券使用门槛，需满"
                    + threshold.stripTrailingZeros().toPlainString() + "元");
            records.add(view);
        }
        long total = records.size();
        int from = (int) Math.min((long) page * size, records.size());
        int to = Math.min(from + size, records.size());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("total", total);
        data.put("records", records.subList(from, to));
        return data;
    }

    private boolean isSettlementOrder(BossOrder order) {
        // 该接口只返回当前处于待结算阶段的订单，避免把招工中/招工结束订单误展示为可核销订单。
        return BossStatus.ORDER_PENDING_SETTLE.equals(order.getOrderStatus());
    }

    private boolean isPendingItem(BaseOrderItem item, List<Settlement> itemSettlements) {
        if (BossStatus.ITEM_CANCELED.equals(item.getStatus())
                || BossStatus.ITEM_CANCEL_BY_BOSS.equals(item.getStatus())
                || BossStatus.ITEM_REJECTED.equals(item.getStatus())) return false;
        boolean paid = itemSettlements.stream().anyMatch(s -> SettlementStatus.PAID.equals(s.getStatus()));
        if (paid) return false;
        // 订单进入待结算时，后端会先生成“待支付”结算单，但报名记录仍可能是“已到岗”。
        // 不能只依赖报名状态，否则正常流程下该接口会始终返回空列表。
        boolean hasPendingSettlement = itemSettlements.stream()
                .anyMatch(s -> SettlementStatus.PENDING.equals(s.getStatus()));
        return hasPendingSettlement
                || BossStatus.ITEM_ON_WORK.equals(item.getStatus())
                || BossStatus.ITEM_PENDING_SETTLE.equals(item.getStatus())
                || BossStatus.ITEM_FINISHED.equals(item.getStatus());
    }

    private BigDecimal estimateWage(BossOrder order, BaseOrderItem item) {
        if (order.getSalary() == null || order.getSalary().signum() <= 0) return BigDecimal.ZERO;
        int days = 1;
        if (item.getWorkDate() != null && item.getFinishDate() != null && !item.getFinishDate().before(item.getWorkDate())) {
            days = (int) (item.getFinishDate().toLocalDate().toEpochDay() - item.getWorkDate().toLocalDate().toEpochDay()) + 1;
        }
        return order.getSalary().multiply(BigDecimal.valueOf(Math.max(1, days))).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateServiceFee(BigDecimal wage) {
        if (serviceFeeRate <= 0) return BigDecimal.ZERO.setScale(2);
        return wage.multiply(BigDecimal.valueOf(serviceFeeRate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> view(UserCoupon record, Coupon coupon) {
        if (coupon == null) throw new EntityNotFoundException("优惠券不存在: " + record.getCouponId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", record.getId());
        result.put("userCouponId", record.getId());
        result.put("couponId", record.getCouponId());
        result.put("name", StringUtils.hasText(coupon.getName()) ? coupon.getName() : coupon.getTitle());
        result.put("title", coupon.getTitle());
        result.put("couponNo", coupon.getCouponNo());
        result.put("type", coupon.getType());
        result.put("target", coupon.getTarget());
        result.put("scope", coupon.getScope());
        result.put("amount", coupon.getAmount());
        result.put("threshold", coupon.getThreshold() != null ? coupon.getThreshold() : coupon.getMinSpend());
        result.put("minSpend", coupon.getMinSpend());
        result.put("discount", coupon.getDiscount());
        result.put("cap", coupon.getCap());
        result.put("status", record.getStatus());
        result.put("expireAt", record.getExpireAt());
        result.put("usedAt", record.getUsedAt());
        result.put("useOrderId", record.getUseOrderId());
        result.put("validMode", coupon.getValidMode());
        result.put("validStart", coupon.getValidStart());
        result.put("validEnd", coupon.getValidEnd());
        result.put("validDays", coupon.getValidDays());
        result.put("grantMode", coupon.getGrantMode());
        result.put("grantStart", coupon.getGrantStart());
        result.put("description", coupon.getDescription());
        return result;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) return null;
        String normalized = status.trim().toUpperCase();
        if (!STATUSES.contains(normalized)) throw new IllegalArgumentException("status 只能是 UNUSED、USED 或 EXPIRED");
        return normalized;
    }

    private boolean isExpired(UserCoupon record, LocalDate today) {
        Date expireAt = record.getExpireAt();
        return expireAt != null && expireAt.toLocalDate().isBefore(today) && !"USED".equals(record.getStatus());
    }
}
