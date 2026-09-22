package com.kuaima.app.domain.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.kuaima.app.domain.boss.entity.BossBalanceRechargeOrder;
import com.kuaima.app.domain.boss.entity.BossMerchantAccount;
import com.kuaima.app.domain.boss.repository.BossBalanceRechargeOrderRepository;
import com.kuaima.app.domain.boss.repository.BossMerchantAccountRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.wechat.service.WechatPayService;

import jakarta.persistence.EntityNotFoundException;

/** 老板钱包微信充值；支付回调确认后才入账，金额单位为元。 */
@Service
public class BossBalancePaymentService {
    private final BossBalanceRechargeOrderRepository orders;
    private final BossMerchantAccountRepository accounts;
    private final UserRepository users;
    private final WechatPayService wechatPay;

    public BossBalancePaymentService(BossBalanceRechargeOrderRepository orders,
            BossMerchantAccountRepository accounts, UserRepository users, WechatPayService wechatPay) {
        this.orders = orders; this.accounts = accounts; this.users = users; this.wechatPay = wechatPay;
    }

    @Transactional
    public Map<String, Object> create(Long bossId, BigDecimal amount, String idempotencyKey) {
        if (amount == null) throw new IllegalArgumentException("amount 不能为空");
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        if (amount.compareTo(new BigDecimal("0.01")) < 0) throw new IllegalArgumentException("充值金额不能低于0.01元");
        User boss = users.findById(bossId).orElseThrow(() -> new EntityNotFoundException("老板账号不存在"));
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency-Key 不能为空");
        String key = "boss-wallet:" + bossId + ":" + idempotencyKey.trim();
        BossBalanceRechargeOrder existing = orders.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if (existing.getAmount().compareTo(amount) != 0) throw new IllegalArgumentException("同一Idempotency-Key不能用于不同充值金额");
            Map<String, Object> oldView = view(existing);
            oldView.put("payParams", existing.getPayParams() == null ? Map.of() : JSON.parseObject(existing.getPayParams()));
            return oldView;
        }
        BossBalanceRechargeOrder order = new BossBalanceRechargeOrder();
        order.setOrderNo("RB" + System.currentTimeMillis() + (int)(Math.random() * 10000));
        order.setBossId(bossId); order.setIdempotencyKey(key); order.setBossName(boss.getRealName()); order.setCompanyName(boss.getCompanyName());
        order.setAmount(amount); order.setPayMethod("wechat"); order.setStatus("pending");
        order = orders.save(order);
        JSONObject payParams = wechatPay.prepay("快马老板钱包充值", order.getOrderNo(), amount, boss.getOpenid());
        order.setPayParams(payParams.toJSONString()); orders.save(order);
        Map<String, Object> result = view(order); result.put("payParams", payParams); return result;
    }

    @Transactional
    public BossBalanceRechargeOrder markPaid(String orderNo, String transactionId) {
        BossBalanceRechargeOrder order = orders.findByOrderNoForUpdate(orderNo)
                .orElseThrow(() -> new EntityNotFoundException("老板钱包充值订单不存在"));
        if ("paid".equals(order.getStatus())) return order;
        BossMerchantAccount account = accounts.findFirstByBossIdAndIsDefaultTrue(order.getBossId()).orElseGet(() -> {
            BossMerchantAccount a = new BossMerchantAccount(); a.setBossId(order.getBossId());
            a.setAccountName(order.getCompanyName() == null ? "企业账户" : order.getCompanyName());
            a.setSubjectName(order.getCompanyName()); a.setBalance(0L); a.setIsDefault(true); a.setStatus("active");
            return accounts.save(a);
        });
        account.setBalance((account.getBalance() == null ? 0L : account.getBalance()) + order.getAmount().movePointRight(2).longValueExact());
        accounts.save(account);
        order.setAccountId(account.getId()); order.setStatus("paid"); order.setTransactionId(transactionId);
        order.setPayTime(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        return orders.save(order);
    }

    public BossBalanceRechargeOrder callbackOrder(String orderNo) {
        return orders.findByOrderNo(orderNo).orElseThrow(() -> new EntityNotFoundException("老板钱包充值订单不存在"));
    }

    private Map<String, Object> view(BossBalanceRechargeOrder order) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("orderNo", order.getOrderNo());
        m.put("amount", order.getAmount()); m.put("status", order.getStatus()); return m;
    }
}
