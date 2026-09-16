package com.kuaima.app.domain.coupon.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.kuaima.app.domain.coupon.entity.Coupon;
import com.kuaima.app.domain.coupon.entity.UserCoupon;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CouponClaimService {
 private static final ZoneId ZONE=ZoneId.of("Asia/Shanghai");
 private final CouponRepository coupons;private final UserCouponRepository claims;private final UserRepository users;
 public CouponClaimService(CouponRepository c,UserCouponRepository uc,UserRepository u){coupons=c;claims=uc;users=u;}
 @Transactional public UserCoupon claim(Long couponId,Long userId){if(userId==null)throw new IllegalArgumentException("userId 不能为空");Coupon c=coupons.findByIdForUpdate(couponId).orElseThrow(()->new EntityNotFoundException("优惠券不存在: "+couponId));if(Boolean.TRUE.equals(c.getDeleted())||Boolean.TRUE.equals(c.getStopped()))throw new IllegalArgumentException("优惠券不可领取");LocalDateTime now=LocalDateTime.now(ZONE);if(c.getGrantStart()!=null&&now.isBefore(c.getGrantStart()))throw new IllegalArgumentException("优惠券尚未开始发放");if("range".equals(c.getValidMode())&&c.getValidEnd()!=null&&!now.isBefore(c.getValidEnd()))throw new IllegalArgumentException("优惠券已结束");User u=users.findById(userId).orElseThrow(()->new EntityNotFoundException("用户不存在: "+userId));if("老板".equals(c.getTarget())&&!UserRole.BOSS.equals(u.getRole()))throw new IllegalArgumentException("该优惠券仅限老板领取");if("零工".equals(c.getTarget())&&!UserRole.USER.equals(u.getRole()))throw new IllegalArgumentException("该优惠券仅限零工领取");if("custom".equals(c.getScope())){List<Long> ids=JSON.parseArray(c.getAssignUsers(),Long.class);if(!ids.contains(userId))throw new IllegalArgumentException("当前用户不在指定发放名单中");}long claimed=claims.countByCouponId(couponId);if(c.getTotal()!=null&&claimed>=c.getTotal())throw new IllegalArgumentException("优惠券已领完");if(c.getLimitPerUser()!=null&&claims.countByCouponIdAndUserId(couponId,userId)>=c.getLimitPerUser())throw new IllegalArgumentException("已达到每人限领数量");UserCoupon uc=new UserCoupon();uc.setUserId(userId);uc.setCouponId(couponId);uc.setStatus("UNUSED");LocalDate expiry=null;if("after".equals(c.getValidMode())&&c.getValidDays()!=null)expiry=LocalDate.now(ZONE).plusDays(c.getValidDays());else if(c.getValidEnd()!=null)expiry=c.getValidEnd().toLocalDate();if(expiry!=null)uc.setExpireAt(Date.valueOf(expiry));UserCoupon saved=claims.save(uc);c.setClaimed((int)(claimed+1));coupons.save(c);return saved;}
}
