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
import com.kuaima.app.domain.review.repository.BossReviewRepository;
import com.kuaima.app.domain.review.entity.BossReview;

@Service
public class BossProfileService {
    private final BossOrderRespository orderRepository;
    private final BaseOrderItemRespository itemRepository;
    private final SettlementRespository settlementRepository;
    private final UserRepository userRepository;
    private final BossReviewRepository reviewRepository;

    public BossProfileService(BossOrderRespository orderRepository,
                              BaseOrderItemRespository itemRepository,
                              SettlementRespository settlementRepository,
                              UserRepository userRepository,
                              BossReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * 个人页统计：统计当前老板全部历史订单，不按分页、不设时间上限。
     * goodRate=三项均为5分的评价条目/全部已完成条目；arrivalRate=正常完工人数/到岗人数；
     * settleRate=完工确认后24小时内已支付人数/全部确认完工人数；totalPayment单位为分。
     */
    public ProfileStats stats(Long bossId) {
        List<BossOrder> orders = orderRepository.findByCreateByOrderByIdDesc(bossId);
        List<Long> orderIds = orders.stream().map(BossOrder::getId).toList();
        List<BaseOrderItem> items = orderIds.isEmpty() ? List.of() : itemRepository.findAllByBossId(bossId);
        List<Settlement> settlements = orderIds.isEmpty() ? List.of() : settlementRepository.findByOrderIdIn(orderIds);

        long arrivedItems = items.stream().filter(i -> i.getWorkDate() != null).count();
        long completedItems = items.stream().filter(i -> BossStatus.ITEM_FINISHED.equals(i.getStatus())
                && !Boolean.TRUE.equals(i.getEarlyLeave())).count();
        int arrivalRate = arrivedItems == 0 ? 0 : percent(completedItems, arrivedItems);
        List<Settlement> paid = settlements.stream().filter(s -> SettlementStatus.PAID.equals(s.getStatus())).toList();
        Map<Long, BaseOrderItem> itemById = items.stream().filter(i -> i.getId() != null)
                .collect(Collectors.toMap(BaseOrderItem::getId, Function.identity(), (a, b) -> a));
        long confirmedFinished = items.stream().filter(i -> finishTime(i) != null).count();
        long settledWithin24h = paid.stream().filter(s -> settledWithin24h(s, itemById)).count();
        int settleRate = confirmedFinished == 0 ? 0 : percent(settledWithin24h, confirmedFinished);
        long totalPayment = paid.stream().mapToLong(s -> s.getTotalAmount() == null ? 0 : s.getTotalAmount()).sum();
        long applicants = items.stream().filter(this::isValid).count();
        long recruiting = orders.stream().filter(o -> BossStatus.ORDER_RECRUITING.equals(o.getOrderStatus())).count();
        long completedOrders = orders.stream().filter(o -> BossStatus.ORDER_COMPLETED.equals(o.getOrderStatus())).count();
        int integrity = userRepository.findById(bossId).map(User::getCreditScore).orElse(0);
        List<Long> completedItemIds = items.stream().filter(i -> BossStatus.ITEM_FINISHED.equals(i.getStatus()))
                .map(BaseOrderItem::getId).filter(java.util.Objects::nonNull).toList();
        List<BossReview> reviews = completedItemIds.isEmpty() ? List.of()
                : reviewRepository.findByItemIdIn(completedItemIds);
        long fiveStarReviews = reviews.stream().filter(r -> Integer.valueOf(5).equals(r.getAttitudeScore())
                && Integer.valueOf(5).equals(r.getSettlementScore())
                && Integer.valueOf(5).equals(r.getEnvironmentScore())).count();
        int goodRate = completedItemIds.isEmpty() ? 0 : percent(fiveStarReviews, completedItemIds.size());
        return new ProfileStats(orders.size(), recruiting, applicants, totalPayment / 100.0,
                Math.max(0, integrity), goodRate, arrivalRate, settleRate, totalPayment, completedOrders);
    }

    private boolean isValid(BaseOrderItem item) {
        return item.getStatus() != null && !BossStatus.ITEM_CANCELED.equals(item.getStatus())
                && !BossStatus.ITEM_CANCEL_BY_BOSS.equals(item.getStatus())
                && !BossStatus.ITEM_REJECTED.equals(item.getStatus());
    }

    private boolean settledWithin24h(Settlement settlement, Map<Long, BaseOrderItem> itemById) {
        if (settlement.getPayTime() == null) return false;
        BaseOrderItem item = itemById.get(settlement.getItemId());
        LocalDateTime finishedAt = finishTime(item);
        if (finishedAt == null) return false;
        return !settlement.getPayTime().isBefore(finishedAt)
                && Duration.between(finishedAt, settlement.getPayTime()).compareTo(Duration.ofHours(24)) <= 0;
    }

    private LocalDateTime finishTime(BaseOrderItem item) {
        if (item == null) return null;
        if (item.getFinishAt() != null) return item.getFinishAt();
        return item.getFinishDate() == null ? null : item.getFinishDate().toLocalDate().atStartOfDay();
    }

    private int percent(long numerator, long denominator) {
        return (int) Math.round(numerator * 100.0 / denominator);
    }
}
