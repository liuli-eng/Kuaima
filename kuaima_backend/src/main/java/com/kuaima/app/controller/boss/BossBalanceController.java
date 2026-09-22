package com.kuaima.app.controller.boss;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossBalanceRechargeOrder;
import com.kuaima.app.domain.boss.entity.BossMerchantAccount;
import com.kuaima.app.domain.boss.repository.BossBalanceRechargeOrderRepository;
import com.kuaima.app.domain.boss.repository.BossMerchantAccountRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;
import com.kuaima.app.domain.wallet.service.BossBalancePaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 老板端「余额查询」模块：商户号账户余额查询 + 充值订单创建/支付回调/列表。
 * 支付逻辑仿照零工端保证金充值（微信/支付宝下单 → 扫码支付 → 支付回调 → 入账）。
 */
@RestController
@RequestMapping("/boss/balance")
@Tag(name = "老板-余额查询", description = "商户账户余额查询、充值订单创建/回调/列表")
@RequiredArgsConstructor
public class BossBalanceController {

    private final BossMerchantAccountRepository accountRepository;
    private final BossBalanceRechargeOrderRepository rechargeOrderRepository;
    private final UserRepository userRepository;
    private final BossBalancePaymentService paymentService;

    private static final BigDecimal MIN_RECHARGE_AMOUNT = new BigDecimal("100.00");
    private static final List<String> ALLOWED_PAY_METHODS = List.of("wechat", "alipay");

    // ---------- 查询 ----------

    @Operation(summary = "查询商户账户余额")
    @GetMapping
    public Result<Map<String, Object>> getBalance(Authentication authentication) {
        Long bossId = requireBossId(authentication);
        User boss = userRepository.findById(bossId).orElse(null);
        BossMerchantAccount account = findAccount(bossId);

        Map<String, Object> data = new LinkedHashMap<>();
        if (account != null) {
            data.put("accountName", account.getAccountName());
            data.put("subjectName", account.getSubjectName());
            data.put("merchantNo", account.getMerchantNo());
            // 余额以「分」存储，前端展示用元
            data.put("balanceFen", account.getBalance() == null ? 0L : account.getBalance());
            data.put("balanceYuan", fenToYuan(account.getBalance()));
            data.put("isDefault", Boolean.TRUE.equals(account.getIsDefault()));
            data.put("status", account.getStatus());
        } else {
            String companyName = boss != null ? boss.getCompanyName() : null;
            data.put("accountName", companyName != null ? companyName : "企业账户");
            data.put("subjectName", companyName);
            data.put("merchantNo", "");
            data.put("balanceFen", 0L);
            data.put("balanceYuan", "0.00");
            data.put("isDefault", true);
            data.put("status", "not_opened");
        }
        return Result.success(data);
    }

    @Operation(summary = "查询当前老板的充值订单列表", description = "可选 status 过滤")
    @GetMapping("/recharge/orders")
    public Result<List<Map<String, Object>>> listRechargeOrders(Authentication authentication,
                                                                @RequestParam(required = false) String status) {
        Long bossId = requireBossId(authentication);
        List<BossBalanceRechargeOrder> orders = (status == null || status.isBlank())
                ? rechargeOrderRepository.findByBossIdOrderByIdDesc(bossId)
                : rechargeOrderRepository.findByBossIdAndStatusOrderByIdDesc(bossId, status);
        List<Map<String, Object>> result = orders.stream()
                .map(this::orderToMap)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    @Operation(summary = "查询单个充值订单详情")
    @GetMapping("/recharge/orders/{orderNo}")
    public Result<Map<String, Object>> getRechargeOrder(Authentication authentication,
                                                        @PathVariable String orderNo) {
        Long bossId = requireBossId(authentication);
        BossBalanceRechargeOrder order = rechargeOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ForbiddenBusinessException("订单不存在"));

        if ("wechat".equalsIgnoreCase(order.getPayMethod())) {
            throw new ForbiddenBusinessException("微信支付订单必须等待微信官方回调确认");
        }
        if (!order.getBossId().equals(bossId)) {
            throw new ForbiddenBusinessException("无权访问该订单");
        }
        return Result.success(orderToMap(order));
    }

    // ---------- 下单 & 支付 ----------

    @Operation(summary = "创建充值订单", description = "创建后返回 orderNo + payQrUrl，前端拉起微信/支付宝扫码支付")
    @PostMapping("/recharge/create")
    @Transactional
    public Result<Map<String, Object>> createRechargeOrder(Authentication authentication,
                                                           @RequestBody Map<String, Object> body,
                                                           @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Long bossId = requireBossId(authentication);
        if ("wechat".equalsIgnoreCase(String.valueOf(body.getOrDefault("payMethod", "wechat")))) {
            return Result.success(paymentService.create(bossId, parseAmount(body.get("amount")), idempotencyKey));
        }
        User boss = userRepository.findById(bossId).orElse(null);

        BigDecimal amount = parseAmount(body.get("amount"));
        String payMethod = String.valueOf(body.getOrDefault("payMethod", "wechat"));

        if (amount == null || amount.compareTo(MIN_RECHARGE_AMOUNT) < 0) {
            throw new ForbiddenBusinessException("最低充值金额为 ¥100");
        }
        if (!ALLOWED_PAY_METHODS.contains(payMethod)) {
            throw new ForbiddenBusinessException("不支持的支付方式");
        }

        BossMerchantAccount account = findAccount(bossId);
        BossBalanceRechargeOrder order = new BossBalanceRechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setBossId(bossId);
        order.setBossName(boss != null ? boss.getRealName() : null);
        order.setCompanyName(boss != null ? boss.getCompanyName() : null);
        order.setAccountId(account != null ? account.getId() : null);
        order.setAmount(amount);
        order.setPayMethod(payMethod);
        order.setStatus("pending");

        // 模拟：对接微信/支付宝统一下单 API 后拿到 payQrUrl
        String qrContent = payMethod + "://kuaima.com/recharge?orderNo=" + order.getOrderNo()
                + "&amount=" + amount.toPlainString();
        order.setPayQrUrl(qrContent);

        order = rechargeOrderRepository.save(order);

        Map<String, Object> data = orderToMap(order);
        data.put("payQrUrl", qrContent);
        data.put("payUrl", qrContent);
        return Result.success(data);
    }

    @Operation(summary = "支付回调（第三方异步通知）", description = "微信/支付宝扫码支付完成后通知；更新订单状态并给商户号入账")
    @PostMapping("/recharge/callback")
    @Transactional
    public Result<Map<String, Object>> payCallback(@RequestBody Map<String, Object> body) {
        String orderNo = String.valueOf(body.getOrDefault("orderNo", ""));
        String transactionId = String.valueOf(body.getOrDefault("transactionId", "CB-" + System.currentTimeMillis()));
        String rawStatus = String.valueOf(body.getOrDefault("status", "paid"));
        boolean success = "paid".equalsIgnoreCase(rawStatus) || "SUCCESS".equalsIgnoreCase(rawStatus);

        if (orderNo.isBlank()) {
            throw new ForbiddenBusinessException("订单号缺失");
        }

        BossBalanceRechargeOrder order = rechargeOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ForbiddenBusinessException("订单不存在"));

        if (success) {
            if ("paid".equals(order.getStatus()) || "manual_confirmed".equals(order.getStatus())) {
                // 幂等：已经入账过了，直接返回成功
                return Result.success(Map.of("orderNo", orderNo, "status", order.getStatus()));
            }
            order.setStatus("paid");
            order.setTransactionId(transactionId);
            order.setPayTime(Timestamp.valueOf(LocalDateTime.now()));

            // 给商户号入账（分单位存储）
            long amountFen = yuanToFen(order.getAmount());
            BossMerchantAccount account = findOrCreateAccount(order.getBossId(), order.getCompanyName());
            account.setBalance((account.getBalance() == null ? 0L : account.getBalance()) + amountFen);
            accountRepository.save(account);
            order.setAccountId(account.getId());
        } else {
            order.setStatus("failed");
            order.setTransactionId(transactionId);
        }

        rechargeOrderRepository.save(order);
        return Result.success(Map.of("orderNo", order.getOrderNo(), "status", order.getStatus()));
    }

    @Operation(summary = "老板主动查询支付结果", description = "前端定时轮询，支付完成后刷新余额")
    @GetMapping("/recharge/query")
    public Result<Map<String, Object>> queryPayResult(Authentication authentication,
                                                      @RequestParam String orderNo) {
        Long bossId = requireBossId(authentication);
        BossBalanceRechargeOrder order = rechargeOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ForbiddenBusinessException("订单不存在"));
        if (!order.getBossId().equals(bossId)) {
            throw new ForbiddenBusinessException("无权访问该订单");
        }
        return Result.success(orderToMap(order));
    }

    // ---------- 工具方法 ----------

    private BossMerchantAccount findAccount(Long bossId) {
        return accountRepository.findFirstByBossIdAndIsDefaultTrue(bossId)
                .orElseGet(() -> accountRepository.findByBossIdOrderByIdDesc(bossId).stream().findFirst()
                        .orElse(null));
    }

    private BossMerchantAccount findOrCreateAccount(Long bossId, String companyName) {
        BossMerchantAccount account = findAccount(bossId);
        if (account != null) return account;
        account = new BossMerchantAccount();
        account.setBossId(bossId);
        account.setAccountName(companyName != null ? companyName : "企业账户");
        account.setSubjectName(companyName);
        account.setMerchantNo("");
        account.setBalance(0L);
        account.setIsDefault(true);
        account.setStatus("active");
        return accountRepository.save(account);
    }

    /** 20260921143025 + 6 位随机 → RB202609211430250001 */
    private static final Random RAND = new Random();
    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = 1000 + RAND.nextInt(9000);
        return "RB" + ts + rand;
    }

    private BigDecimal parseAmount(Object raw) {
        if (raw == null) return null;
        try {
            return new BigDecimal(String.valueOf(raw));
        } catch (Exception e) {
            return null;
        }
    }

    private static final BigDecimal FEN_PER_YUAN = new BigDecimal("100");
    private long yuanToFen(BigDecimal yuan) {
        if (yuan == null) return 0L;
        return yuan.multiply(FEN_PER_YUAN).longValue();
    }

    private String fenToYuan(Long fen) {
        if (fen == null) return "0.00";
        return new BigDecimal(fen).divide(FEN_PER_YUAN, 2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private Map<String, Object> orderToMap(BossBalanceRechargeOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo());
        m.put("bossId", o.getBossId());
        m.put("bossName", o.getBossName());
        m.put("companyName", o.getCompanyName());
        m.put("amount", o.getAmount());
        m.put("payMethod", o.getPayMethod());
        m.put("status", o.getStatus());
        m.put("payTime", o.getPayTime() != null ? o.getPayTime().toString() : null);
        m.put("transactionId", o.getTransactionId());
        m.put("remark", o.getRemark());
        m.put("operatorName", o.getOperatorName());
        m.put("operatorTime", o.getOperatorTime() != null ? o.getOperatorTime().toString() : null);
        m.put("createdAt", o.getTimestamp() != null ? o.getTimestamp().toString() : null);
        return m;
    }

    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser
                && UserRole.BOSS.equals(loginUser.role()) && loginUser.id() != null) {
            return loginUser.id();
        }
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}
