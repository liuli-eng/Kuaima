package com.kuaima.app.domain.coupon.service;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.coupon.entity.Coupon;
import com.kuaima.app.domain.coupon.entity.UserCoupon;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BossCouponService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<String> STATUSES = Set.of("UNUSED", "USED", "EXPIRED");
    private final UserCouponRepository userCoupons;
    private final CouponRepository coupons;

    public BossCouponService(UserCouponRepository userCoupons, CouponRepository coupons) {
        this.userCoupons = userCoupons;
        this.coupons = coupons;
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
        BigDecimal orderAmount = BigDecimal.valueOf(order.getSalary())
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

    private Map<String, Object> view(UserCoupon record, Coupon coupon) {
        if (coupon == null) throw new EntityNotFoundException("优惠券不存在: " + record.getCouponId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userCouponId", record.getId());
        result.put("couponId", record.getCouponId());
        result.put("name", StringUtils.hasText(coupon.getName()) ? coupon.getName() : coupon.getTitle());
        result.put("amount", coupon.getAmount());
        result.put("threshold", coupon.getThreshold() != null ? coupon.getThreshold() : coupon.getMinSpend());
        result.put("status", record.getStatus());
        result.put("expireAt", record.getExpireAt());
        result.put("usedAt", record.getUsedAt());
        result.put("useOrderId", record.getUseOrderId());
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
