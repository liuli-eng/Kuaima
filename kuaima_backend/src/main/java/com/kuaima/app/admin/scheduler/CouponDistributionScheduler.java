package com.kuaima.app.admin.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.kuaima.app.admin.service.AdminCouponService;
import com.kuaima.app.domain.coupon.repository.CouponRepository;

@Component
public class CouponDistributionScheduler {
    private static final Logger log = LoggerFactory.getLogger(CouponDistributionScheduler.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final CouponRepository coupons;
    private final AdminCouponService service;
    public CouponDistributionScheduler(CouponRepository coupons, AdminCouponService service) {
        this.coupons = coupons; this.service = service;
    }
    @Scheduled(fixedDelay = 60000)
    public void distributeDue() {
        for (Long id : coupons.findDueAutoDistributionIds(LocalDateTime.now(ZONE))) {
            try {
                service.distributeDue(id);
            } catch (RuntimeException exception) {
                log.error("定时发放优惠券失败，couponId={}", id, exception);
            }
        }
    }
}
