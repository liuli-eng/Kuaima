package com.kuaima.app.domain.boss.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.model.BossProfileModels.ProfileStats;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

@Service
public class BossProfileService {
    private final BossOrderRespository orderRepository;
    private final BaseOrderItemRespository itemRepository;
    private final SettlementRespository settlementRepository;
    private final UserRepository userRepository;

    public BossProfileService(BossOrderRespository orderRepository,
                              BaseOrderItemRespository itemRepository,
                              SettlementRespository settlementRepository,
                              UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
    }

    /**
     * 个人页统计：统计当前老板全部历史订单，不按分页、不设时间上限。
     * goodRate 因当前模型没有评价表暂返回 0；arrivalRate=已完成报名/已录用及之后有效报名；
     * settleRate=完成日期到支付时间不超过24小时的已支付结算单/已完成报名记录；totalPayment单位为分。
     */
    public ProfileStats stats(Long bossId) {
        List<BossOrder> orders = orderRepository.findByCreateByOrderByIdDesc(bossId);
        List<Long> orderIds = orders.stream().map(BossOrder::getId).toList();
        List<BaseOrderItem> items = orderIds.isEmpty() ? List.of() : itemRepository.findAllByBossId(bossId);
        List<Settlement> settlements = orderIds.isEmpty() ? List.of() : settlementRepository.findByOrderIdIn(orderIds);

        long validHired = items.stream().filter(this::isValid).filter(i ->
                BossStatus.ITEM_HIRED.equals(i.getStatus()) || BossStatus.ITEM_ON_WORK.equals(i.getStatus())
                        || BossStatus.ITEM_FINISHED.equals(i.getStatus())).count();
        long completedItems = items.stream().filter(i -> BossStatus.ITEM_FINISHED.equals(i.getStatus())).count();
        int arrivalRate = validHired == 0 ? 0 : percent(completedItems, validHired);
        List<Settlement> paid = settlements.stream().filter(s -> SettlementStatus.PAID.equals(s.getStatus())).toList();
        Map<Long, BaseOrderItem> itemById = items.stream().filter(i -> i.getId() != null)
                .collect(Collectors.toMap(BaseOrderItem::getId, Function.identity(), (a, b) -> a));
        long settledWithin24h = paid.stream().filter(s -> settledWithin24h(s, itemById)).count();
        int settleRate = completedItems == 0 ? 0 : percent(settledWithin24h, completedItems);
        long totalPayment = paid.stream().mapToLong(s -> s.getTotalAmount() == null ? 0 : s.getTotalAmount()).sum();
        long applicants = items.stream().filter(this::isValid).count();
        long recruiting = orders.stream().filter(o -> BossStatus.ORDER_RECRUITING.equals(o.getOrderStatus())).count();
        long completedOrders = orders.stream().filter(o -> BossStatus.ORDER_COMPLETED.equals(o.getOrderStatus())).count();
        int integrity = userRepository.findById(bossId).map(User::getCreditScore).orElse(0);
        return new ProfileStats(orders.size(), recruiting, applicants, totalPayment / 100.0,
                Math.max(0, integrity), 0, arrivalRate, settleRate, totalPayment, completedOrders);
    }

    private boolean isValid(BaseOrderItem item) {
        return item.getStatus() != null && !BossStatus.ITEM_CANCELED.equals(item.getStatus())
                && !BossStatus.ITEM_CANCEL_BY_BOSS.equals(item.getStatus())
                && !BossStatus.ITEM_REJECTED.equals(item.getStatus());
    }

    private boolean settledWithin24h(Settlement settlement, Map<Long, BaseOrderItem> itemById) {
        if (settlement.getPayTime() == null) return false;
        BaseOrderItem item = itemById.get(settlement.getItemId());
        if (item == null || item.getFinishDate() == null) return false;
        LocalDateTime finishedAt = item.getFinishDate().toLocalDate().atStartOfDay();
        return !settlement.getPayTime().isBefore(finishedAt)
                && Duration.between(finishedAt, settlement.getPayTime()).toHours() <= 24;
    }

    private int percent(long numerator, long denominator) {
        return (int) Math.round(numerator * 100.0 / denominator);
    }
}
