package com.kuaima.app.domain.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.user.service.CreditScoreService;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.entity.Wallet;
import com.kuaima.app.domain.wallet.model.PendingSettlementModels.PendingSettlementItem;
import com.kuaima.app.domain.wallet.model.PendingSettlementModels.PendingSettlementOrder;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.admin.entity.AdminSetting;
import com.kuaima.app.admin.repository.AdminSettingRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 结算服务：老板对"已完成"的报名记录发起结算 -> 系统按 订单工资 × 工作天数 自动算工资(分)，
 * 并叠加平台服务费 -> 生成"待支付"结算单 -> 老板模拟支付成功后工资入零工钱包。
 * 服务费默认 0（费率规则待定，见 suma.settle.service-fee-rate）。
 */
@Service
public class SettlementService {

    private final SettlementRespository settlementRepository;
    private final BossOrderRespository orderRepository;
    private final BaseOrderItemRespository itemRepository;
    private final WalletService walletService;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final AdminSettingRepository adminSettingRepository;
    private final CreditScoreService creditScoreService;

    /** 平台服务费率(% of wage)，规则待定，默认 0 */
    @Value("${kuaima.settle.service-fee-rate:0}")
    private BigDecimal serviceFeeRate;

    public SettlementService(SettlementRespository settlementRepository,
                             BossOrderRespository orderRepository,
                            BaseOrderItemRespository itemRepository,
                            WalletService walletService,
                             MessageService messageService,
                             UserRepository userRepository) {
        this(settlementRepository, orderRepository, itemRepository, walletService, messageService, userRepository, null, null);
    }

    public SettlementService(SettlementRespository settlementRepository,
                             BossOrderRespository orderRepository,
                             BaseOrderItemRespository itemRepository,
                             WalletService walletService,
                             MessageService messageService,
                             UserRepository userRepository,
                             AdminSettingRepository adminSettingRepository) {
        this(settlementRepository, orderRepository, itemRepository, walletService, messageService, userRepository,
                adminSettingRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public SettlementService(SettlementRespository settlementRepository,
                             BossOrderRespository orderRepository,
                             BaseOrderItemRespository itemRepository,
                             WalletService walletService,
                             MessageService messageService,
                             UserRepository userRepository,
                             AdminSettingRepository adminSettingRepository,
                             CreditScoreService creditScoreService) {
        this.settlementRepository = settlementRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.walletService = walletService;
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.adminSettingRepository = adminSettingRepository;
        this.creditScoreService = creditScoreService;
    }

    /**
     * 创建结算单：报名记录必须处于「待结算」（历史兼容「已完成」）；
     * 结算单创建成功后将报名记录推进为「已完成」（老板点击"去结算"后岗位状态变为已完成）。
     * 同一报名记录不允许重复结算。
     *
     * @param workDays 实际工作天数，为空时由 到岗日~完成日 自动推导（最小 1 天）
     */
    @Transactional
    public Settlement createSettlement(Long itemId, Integer workDays) {
        BaseOrderItem item = getItemOrThrow(itemId);
        if (!BossStatus.ITEM_PENDING_SETTLE.equals(item.getStatus())
                && !BossStatus.ITEM_FINISHED.equals(item.getStatus())) {
            throw new IllegalStateException("仅待结算的报名记录可以发起结算");
        }
        if (settlementRepository.existsByItemIdAndStatusIn(itemId,
                List.of(SettlementStatus.PENDING, SettlementStatus.PAID))) {
            throw new IllegalStateException("该报名记录已存在待支付/已支付的结算单，请勿重复结算");
        }
        BossOrder order = orderRepository.findById(item.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("订单不存在: " + item.getOrderId()));
        if (order.getSalary() == null || order.getSalary().signum() <= 0) {
            throw new IllegalStateException("订单工资未设置，无法结算");
        }
        int days = resolveWorkDays(item, workDays);
        BigDecimal wage = order.getSalary().multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceFee = calculateServiceFee(wage);
        BigDecimal total = wage.add(serviceFee);

        Settlement s = new Settlement();
        s.setItemId(itemId);
        s.setOrderId(item.getOrderId());
        s.setWorkerId(item.getUserId());
        s.setWorkDays(days);
        s.setWage(wage);
        s.setServiceFee(serviceFee);
        s.setTotalAmount(total);
        s.setStatus(SettlementStatus.PENDING);
        Settlement saved = settlementRepository.save(s);

        if (!BossStatus.ITEM_FINISHED.equals(item.getStatus())) {
            item.setStatus(BossStatus.ITEM_FINISHED);
            itemRepository.save(item);
        }
        return saved;
    }

    /**
     * 模拟支付：待支付 -> 已支付；工资入零工钱包并记流水；服务费归平台（记录在结算单上）。
     */
    @Transactional
    public Settlement mockPay(Long settlementId) {
        Settlement s = getSettlementOrThrow(settlementId);
        if (SettlementStatus.PAID.equals(s.getStatus())) {
            // 支付回调/前端重试必须幂等：不重复入账，只补偿历史状态同步。
            synchronizePaidItemAndOrder(s);
            return s;
        }
        if (!SettlementStatus.PENDING.equals(s.getStatus())) {
            throw new IllegalStateException("仅待支付的结算单可以支付");
        }
        s.setStatus(SettlementStatus.PAID);
        s.setPayNo("MOCK" + System.currentTimeMillis());
        s.setPayTime(LocalDateTime.now());
        settlementRepository.save(s);

        synchronizePaidItemAndOrder(s);
        applyBossCredit(s);

        // 工资入零工钱包
        walletService.credit(s.getWorkerId(), s.getWage(), WalletService.BIZ_WAGE,
                s.getId(), "工资结算 orderId=" + s.getOrderId() + " 天数=" + s.getWorkDays());
        // 结算到账：通知零工
        messageService.sendToUser(s.getWorkerId(), UserRole.USER, MessageType.SETTLE_PAID, "工资已到账",
                "您的工资 " + s.getWage().stripTrailingZeros().toPlainString() + " 元已到账，可在钱包中查看或提现。",
                BizType.SETTLE, s.getId(), java.util.Map.of("wage", s.getWage(), "orderId", s.getOrderId()));
        return s;
    }

    private void applyBossCredit(Settlement settlement) {
        if (creditScoreService == null || settlement == null || settlement.getOrderId() == null) return;
        BossOrder order = orderRepository.findById(settlement.getOrderId()).orElse(null);
        if (order == null || order.getCreateBy() == null) return;
        if (settlement.getWage() != null && settlement.getWage().compareTo(new BigDecimal("20")) > 0) {
            creditScoreService.adjust(order.getCreateBy(), CreditScoreService.BOSS_CREDIT, 3,
                    "BOSS_ORDER_SETTLED", "SETTLEMENT", "BOSS_ORDER_SETTLED:" + settlement.getId(),
                    "订单结算成功且金额大于20元");
            if (bossCompletionRateAtLeast90(order.getCreateBy(), settlement.getPayTime())) {
                creditScoreService.adjust(order.getCreateBy(), CreditScoreService.BOSS_CREDIT, 3,
                        "BOSS_COMPLETION_RATE_90D", "SETTLEMENT",
                        "BOSS_COMPLETION_RATE_90D:" + settlement.getId(), "近90天完单率达到90%，完成订单额外加分");
            }
        }
        BaseOrderItem item = itemRepository.findById(settlement.getItemId()).orElse(null);
        if (item != null && item.getFinishAt() != null && settlement.getPayTime() != null
                && !settlement.getPayTime().isBefore(item.getFinishAt())
                && !settlement.getPayTime().isAfter(item.getFinishAt().plusHours(1))) {
            creditScoreService.adjust(order.getCreateBy(), CreditScoreService.BOSS_CREDIT, 2,
                    "BOSS_SETTLE_WITHIN_1H", "SETTLEMENT", "BOSS_SETTLE_WITHIN_1H:" + settlement.getId(),
                    "完工后1小时内完成结算");
        }
    }

    private boolean bossCompletionRateAtLeast90(Long bossId, LocalDateTime now) {
        if (bossId == null || now == null) return false;
        LocalDateTime start = now.minusDays(90);
        int total = 0;
        int completed = 0;
        for (BaseOrderItem item : itemRepository.findByBossIdJoinOrder(bossId)) {
            if (item.getHireDate() == null) continue;
            LocalDateTime occurred = item.getFinishAt() != null
                    ? item.getFinishAt() : item.getHireDate().toLocalDate().atStartOfDay();
            if (occurred.isBefore(start) || occurred.isAfter(now)) continue;
            total++;
            if (BossStatus.ITEM_FINISHED.equals(item.getStatus())
                    || settlementRepository.existsByItemIdAndStatusIn(item.getId(), List.of(SettlementStatus.PAID))) {
                completed++;
            }
        }
        return total > 0 && completed * 100 >= total * 90;
    }

    private void synchronizePaidItemAndOrder(Settlement settlement) {
        BaseOrderItem paidItem = itemRepository.findById(settlement.getItemId()).orElse(null);
        if (paidItem != null && (BossStatus.ITEM_ON_WORK.equals(paidItem.getStatus())
                || BossStatus.ITEM_PENDING_SETTLE.equals(paidItem.getStatus()))) {
            paidItem.setStatus(BossStatus.ITEM_FINISHED);
            itemRepository.save(paidItem);
        }
        markOrderCompletedIfSettled(settlement.getOrderId());
    }

    /** 所有有效录取人员均已完成且结算单全部支付后，订单自动变为已完成。 */
    private void markOrderCompletedIfSettled(Long orderId) {
        BossOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !BossStatus.ORDER_PENDING_SETTLE.equals(order.getOrderStatus())) {
            return;
        }
        List<BaseOrderItem> activeItems = itemRepository.findByOrderId(orderId).stream()
                .filter(i -> BossStatus.ITEM_HIRED.equals(i.getStatus())
                        || BossStatus.ITEM_ON_WORK.equals(i.getStatus())
                        || BossStatus.ITEM_PENDING_SETTLE.equals(i.getStatus())
                        || BossStatus.ITEM_FINISHED.equals(i.getStatus()))
                .toList();
        if (activeItems.isEmpty() || activeItems.stream().anyMatch(i ->
                !BossStatus.ITEM_FINISHED.equals(i.getStatus()))) {
            return;
        }
        List<Settlement> settlements = settlementRepository.findByOrderIdOrderByIdDesc(orderId);
        boolean allPaid = activeItems.stream().allMatch(item -> settlements.stream().anyMatch(settlement ->
                item.getId().equals(settlement.getItemId())
                        && SettlementStatus.PAID.equals(settlement.getStatus())));
        if (allPaid) {
            order.setOrderStatus(BossStatus.ORDER_COMPLETED);
            orderRepository.save(order);
        }
    }

    /** 按订单查结算单 */
    public List<Settlement> listByOrder(Long orderId) {
        return settlementRepository.findByOrderIdOrderByIdDesc(orderId);
    }

    /** 按零工查结算单 */
    public List<Settlement> listByWorker(Long userId) {
        return settlementRepository.findByWorkerIdOrderByIdDesc(userId);
    }

    /** 零工钱包查询（复用钱包服务） */
    public Wallet getWorkerWallet(Long userId) {
        return walletService.getOrCreateWallet(userId);
    }

    /** 结算单详情 */
    public Settlement getSettlementDetail(Long id) {
        return getSettlementOrThrow(id);
    }

    /** 查询老板全部岗位的待结算报名聚合数据。 */
    @Transactional(readOnly = true)
    public List<PendingSettlementOrder> listPendingByBoss(Long bossId) {
        if (bossId == null) {
            throw new IllegalArgumentException("老板用户ID不能为空");
        }
        List<BossOrder> orders = orderRepository.findByCreateByOrderByIdDesc(bossId);
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<Long, BossOrder> ordersById = orders.stream()
                .collect(java.util.stream.Collectors.toMap(BossOrder::getId, item -> item));
        List<BaseOrderItem> allItems = itemRepository.findByBossIdJoinOrder(bossId);
        List<Long> itemIds = allItems.stream().map(BaseOrderItem::getId).filter(Objects::nonNull).toList();
        Map<Long, List<Settlement>> settlementsByItem = itemIds.isEmpty()
                ? Map.of()
                : settlementRepository.findByItemIdInOrderByIdDesc(itemIds).stream()
                        .collect(java.util.stream.Collectors.groupingBy(Settlement::getItemId));
        Map<Long, User> usersById = loadUsers(allItems);
        Map<Long, List<PendingSettlementItem>> pendingByOrder = new LinkedHashMap<>();

        for (BaseOrderItem item : allItems) {
            BossOrder order = ordersById.get(item.getOrderId());
            if (order == null) continue;
            List<Settlement> itemSettlements = settlementsByItem.getOrDefault(item.getId(), List.of());
            Settlement pending = itemSettlements.stream()
                    .filter(s -> SettlementStatus.PENDING.equals(s.getStatus()))
                    .findFirst().orElse(null);
            boolean paid = itemSettlements.stream().anyMatch(s -> SettlementStatus.PAID.equals(s.getStatus()));
            if (paid && pending == null) continue;
            if (pending == null
                    && !BossStatus.ITEM_PENDING_SETTLE.equals(item.getStatus())
                    && !BossStatus.ITEM_FINISHED.equals(item.getStatus())) continue;
            PendingSettlementItem view = pending == null
                    ? estimateItem(item, order, usersById.get(item.getUserId()))
                    : toPendingItem(pending, usersById.get(item.getUserId()));
            pendingByOrder.computeIfAbsent(order.getId(), ignored -> new ArrayList<>()).add(view);
        }

        return orders.stream()
                .filter(order -> pendingByOrder.containsKey(order.getId()))
                .map(order -> toPendingOrder(order, pendingByOrder.get(order.getId())))
                .toList();
    }

    private Map<Long, User> loadUsers(List<BaseOrderItem> items) {
        List<Long> userIds = items.stream().map(BaseOrderItem::getUserId)
                .filter(Objects::nonNull).distinct().toList();
        if (userIds.isEmpty()) return Map.of();
        return userRepository.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, item -> item));
    }

    private PendingSettlementItem estimateItem(BaseOrderItem item, BossOrder order, User worker) {
        if (order.getSalary() == null || order.getSalary().signum() <= 0) {
            throw new IllegalStateException("订单工资未设置，无法计算待结算金额: " + order.getId());
        }
        int days = resolveWorkDays(item, null);
        BigDecimal wage = order.getSalary().multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = wage.add(calculateServiceFee(wage));
        return new PendingSettlementItem(item.getId(), item.getUserId(), workerName(worker), null, null,
                total, toFen(total), days);
    }

    private PendingSettlementItem toPendingItem(Settlement settlement, User worker) {
        BigDecimal amount = settlement.getTotalAmount() == null ? BigDecimal.ZERO : settlement.getTotalAmount();
        return new PendingSettlementItem(settlement.getItemId(), settlement.getWorkerId(), workerName(worker),
                settlement.getId(), settlement.getStatus(), amount, toFen(amount),
                settlement.getWorkDays());
    }

    private PendingSettlementOrder toPendingOrder(BossOrder order, List<PendingSettlementItem> items) {
        BigDecimal amount = items.stream().map(PendingSettlementItem::amount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<String> workers = items.stream().map(PendingSettlementItem::workerName)
                .filter(org.springframework.util.StringUtils::hasText).distinct().toList();
        boolean hasUncreated = items.stream().anyMatch(item -> item.settlementId() == null);
        boolean hasPending = items.stream().anyMatch(item -> item.settlementId() != null);
        String status = hasUncreated && hasPending ? "partial" : "waiting";
        return new PendingSettlementOrder(order.getId(), order.getId(), order.getDate(),
                org.springframework.util.StringUtils.hasText(order.getOrderTitle())
                        ? order.getOrderTitle() : order.getPostion(),
                amount, toFen(amount), items.size(), workers, status,
                "partial".equals(status) ? "部分结算" : "待结算", items);
    }

    private Long toFen(BigDecimal amount) {
        return amount == null ? null : amount.movePointRight(2).longValueExact();
    }

    private String workerName(User worker) {
        if (worker == null) return "零工";
        if (org.springframework.util.StringUtils.hasText(worker.getNickname())) return worker.getNickname();
        if (org.springframework.util.StringUtils.hasText(worker.getRealName())) return worker.getRealName();
        return "零工#" + worker.getId();
    }


    // ==================== 内部方法 ====================

    private int resolveWorkDays(BaseOrderItem item, Integer workDays) {
        if (workDays != null && workDays > 0) {
            return workDays;
        }
        Date work = item.getWorkDate();
        Date finish = item.getFinishDate();
        if (work != null && finish != null && !finish.before(work)) {
            long span = ChronoUnit.DAYS.between(work.toLocalDate(), finish.toLocalDate()) + 1;
            return Math.max(1, (int) span);
        }
        return 1;
    }

    private BigDecimal calculateServiceFee(BigDecimal wage) {
        BigDecimal rate = configuredServiceFeeRate();
        if (rate.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return wage
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** 后台规则优先；未配置时兼容 kuaima.settle.service-fee-rate。 */
    private BigDecimal configuredServiceFeeRate() {
        BigDecimal fallback = serviceFeeRate == null ? BigDecimal.ZERO : serviceFeeRate.max(BigDecimal.ZERO);
        if (adminSettingRepository == null) return fallback;
        boolean enabled = adminSettingRepository.findById("rules.platformFeeEnabled")
                .map(AdminSetting::getSettingValue)
                .map(String::trim)
                .map(Boolean::parseBoolean)
                .orElse(fallback.signum() > 0);
        if (!enabled) return BigDecimal.ZERO;
        return adminSettingRepository.findById("rules.feeRate")
                .map(AdminSetting::getSettingValue)
                .map(String::trim)
                .map(this::parseRate)
                .orElse(fallback);
    }

    private BigDecimal parseRate(String value) {
        BigDecimal fallback = serviceFeeRate == null ? BigDecimal.ZERO : serviceFeeRate.max(BigDecimal.ZERO);
        try { return new BigDecimal(value).max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)); }
        catch (RuntimeException e) { return fallback; }
    }

    private Settlement getSettlementOrThrow(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("结算单不存在: " + id));
    }

    private BaseOrderItem getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("报名记录不存在: " + id));
    }
}
