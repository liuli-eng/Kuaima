package com.kuaima.app.domain.coupon.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.coupon.entity.Coupon;
import com.kuaima.app.domain.coupon.entity.UserCoupon;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BossCouponServiceTests {
    private UserCouponRepository userCoupons;
    private CouponRepository coupons;
    private BossCouponService service;

    @BeforeEach
    void setUp() {
        userCoupons = mock(UserCouponRepository.class);
        coupons = mock(CouponRepository.class);
        service = new BossCouponService(userCoupons, coupons);
    }

    @Test
    void listReturnsClaimAndCouponDetails() {
        UserCoupon record = unusedRecord(); record.setId(11L); record.setCouponId(21L);
        Coupon coupon = coupon(); coupon.setId(21L); coupon.setName("招工满减券");
        when(userCoupons.findByUserId(7L)).thenReturn(List.of(record));
        when(coupons.findAllById(List.of(21L))).thenReturn(List.of(coupon));

        var result = service.list(7L, "UNUSED");

        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).get("userCouponId"));
        assertEquals("招工满减券", result.get(0).get("name"));
        assertEquals(new BigDecimal("100"), result.get(0).get("threshold"));
    }

    @Test
    void redeemValidatesOwnerAndThreshold() {
        UserCoupon record = unusedRecord(); record.setUserId(8L);
        when(userCoupons.findByIdForUpdate(11L)).thenReturn(Optional.of(record));
        assertThrows(ForbiddenBusinessException.class, () -> service.redeem(7L, 11L, order(30, 1, 1)));

        record.setUserId(7L); record.setCouponId(21L);
        when(coupons.findByIdForUpdate(21L)).thenReturn(Optional.of(coupon()));
        var error = assertThrows(IllegalArgumentException.class, () -> service.redeem(7L, 11L, order(30, 1, 1)));
        assertTrue(error.getMessage().contains("使用门槛"));
        verify(userCoupons, never()).save(argThat(v -> "USED".equals(v.getStatus())));
    }

    @Test
    void redeemRecordsOrderAndUsageTime() {
        UserCoupon record = unusedRecord(); record.setCouponId(21L);
        Coupon coupon = coupon(); coupon.setUsed(2);
        BossOrder order = order(100, 1, 1); order.setId(31L);
        when(userCoupons.findByIdForUpdate(11L)).thenReturn(Optional.of(record));
        when(coupons.findByIdForUpdate(21L)).thenReturn(Optional.of(coupon));

        service.redeem(7L, 11L, order);

        assertEquals("USED", record.getStatus()); assertEquals(31L, record.getUseOrderId());
        assertNotNull(record.getUsedAt()); assertEquals(3, coupon.getUsed());
    }

    @Test
    void concurrentSecondRedemptionIsRejectedAfterLockedFirstUse() throws Exception {
        UserCoupon record = unusedRecord(); record.setCouponId(21L);
        Coupon coupon = coupon(); BossOrder first = order(100, 1, 1); first.setId(31L);
        BossOrder second = order(100, 1, 1); second.setId(32L);
        ReentrantLock databaseRowLock = new ReentrantLock();
        when(userCoupons.findByIdForUpdate(11L)).thenAnswer(invocation -> {
            databaseRowLock.lock();
            return Optional.of(record);
        });
        when(userCoupons.save(record)).thenAnswer(invocation -> {
            if (databaseRowLock.isHeldByCurrentThread()) databaseRowLock.unlock();
            return record;
        });
        when(coupons.findByIdForUpdate(21L)).thenReturn(Optional.of(coupon));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Future<String> firstResult = executor.submit(() -> redeemConcurrently(start, databaseRowLock, first));
        Future<String> secondResult = executor.submit(() -> redeemConcurrently(start, databaseRowLock, second));
        start.countDown();
        List<String> results = List.of(firstResult.get(3, TimeUnit.SECONDS), secondResult.get(3, TimeUnit.SECONDS));
        executor.shutdownNow();

        assertTrue(results.contains("SUCCESS"));
        assertTrue(results.contains("优惠券已使用或不可用"));
        assertTrue(List.of(31L, 32L).contains(record.getUseOrderId()));
        verify(userCoupons, times(2)).findByIdForUpdate(11L);
        verify(userCoupons, times(1)).save(record);
    }

    private UserCoupon unusedRecord() {
        UserCoupon record = new UserCoupon(); record.setId(11L); record.setUserId(7L); record.setStatus("UNUSED");
        record.setExpireAt(Date.valueOf(LocalDate.now().plusDays(1))); return record;
    }
    private Coupon coupon() {
        Coupon coupon = new Coupon(); coupon.setId(21L); coupon.setAmount(new BigDecimal("20"));
        coupon.setThreshold(new BigDecimal("100")); return coupon;
    }
    private BossOrder order(int salary, int duration, int people) {
        BossOrder order = new BossOrder(); order.setSalary(salary); order.setDuration(duration); order.setOrderNum(people); return order;
    }
    private String redeemConcurrently(CountDownLatch start, ReentrantLock lock, BossOrder order) throws InterruptedException {
        start.await();
        try { service.redeem(7L, 11L, order); return "SUCCESS"; }
        catch (IllegalArgumentException e) { return e.getMessage(); }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }
}
