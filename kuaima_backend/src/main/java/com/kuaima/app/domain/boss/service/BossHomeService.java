package com.kuaima.app.domain.boss.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.entity.BossRecruitAccount;
import com.kuaima.app.domain.boss.model.BossHomeModels.Account;
import com.kuaima.app.domain.boss.model.BossHomeModels.Overview;
import com.kuaima.app.domain.boss.model.BossHomeModels.Schedule;
import com.kuaima.app.domain.boss.model.BossHomeModels.ScheduleDay;
import com.kuaima.app.domain.boss.model.BossHomeModels.ScheduleRecord;
import com.kuaima.app.domain.boss.model.BossHomeModels.ScheduleStats;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.repository.BossRecruitAccountRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

@Service
public class BossHomeService {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final BossOrderRespository orderRepository;
    private final BaseOrderItemRespository itemRepository;
    private final SettlementRespository settlementRepository;
    private final BossRecruitAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BossAttendanceCodeService attendanceCodeService;

    public BossHomeService(BossOrderRespository orderRepository,
                           BaseOrderItemRespository itemRepository,
                           SettlementRespository settlementRepository,
                           BossRecruitAccountRepository accountRepository,
                           UserRepository userRepository) {
        this(orderRepository, itemRepository, settlementRepository, accountRepository, userRepository, null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    public BossHomeService(BossOrderRespository orderRepository,
                           BaseOrderItemRespository itemRepository,
                           SettlementRespository settlementRepository,
                           BossRecruitAccountRepository accountRepository,
                           UserRepository userRepository, BossAttendanceCodeService attendanceCodeService) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.settlementRepository = settlementRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.attendanceCodeService = attendanceCodeService;
    }

    @Transactional
    public Overview overview(Long bossId, String city, Long accountId) {
        String resolvedCity = resolveCity(bossId, city);
        BossRecruitAccount account = resolveAccount(bossId, accountId);
        LocalDate today = LocalDate.now(SERVER_ZONE);
        List<Long> owners = accountScope(bossId, accountId);
        List<ScheduleDay> days = List.of(
                scheduleDay(owners, today.minusDays(1), "昨天"),
                scheduleDay(owners, today, "今天"),
                scheduleDay(owners, today.plusDays(1), "明天"),
                scheduleDay(owners, today.plusDays(2), "后天"));
        long nearbyWorkers = StringUtils.hasText(resolvedCity)
                ? userRepository.countByRoleAndCity(UserRole.USER, resolvedCity)
                : userRepository.countByRole(UserRole.USER);
        // 当前数据模型没有报名响应分钟字段，不能伪造“最快接单时间”，暂无可靠数据时返回 0。
        return new Overview(resolvedCity, nearbyWorkers, 0, account.getId(), toAccount(account, bossId), days);
    }

    @Transactional(readOnly = true)
    public Schedule schedule(Long bossId, LocalDate date, Long accountId) {
        List<Long> owners = accountScope(bossId, accountId);
        List<BossOrder> orders = ordersOn(owners, date);
        List<Long> orderIds = orders.stream().map(BossOrder::getId).toList();
        List<BaseOrderItem> items = orderIds.isEmpty() ? List.of() : itemRepository.findByOrderIdIn(orderIds);
        List<Long> itemIds = items.stream().map(BaseOrderItem::getId).toList();
        List<Settlement> settlements = itemIds.isEmpty() ? List.of()
                : settlementRepository.findByItemIdInOrderByIdDesc(itemIds);
        Map<Long, List<BaseOrderItem>> itemsByOrder = items.stream()
                .collect(Collectors.groupingBy(BaseOrderItem::getOrderId));

        long accepted = count(items, List.of(BossStatus.ITEM_HIRED, BossStatus.ITEM_ON_WORK,
                BossStatus.ITEM_PENDING_SETTLE, BossStatus.ITEM_FINISHED));
        long arrived = count(items, List.of(BossStatus.ITEM_ON_WORK, BossStatus.ITEM_PENDING_SETTLE,
                BossStatus.ITEM_FINISHED));
        long working = count(items, List.of(BossStatus.ITEM_ON_WORK));
        long finished = count(items, List.of(BossStatus.ITEM_PENDING_SETTLE, BossStatus.ITEM_FINISHED));
        // 结算口径：待支付和已支付均表示已生成有效结算单；按报名 item 去重，排除已取消。
        long settled = settlements.stream()
                .filter(s -> SettlementStatus.PENDING.equals(s.getStatus()) || SettlementStatus.PAID.equals(s.getStatus()))
                .map(Settlement::getItemId).filter(java.util.Objects::nonNull).distinct().count();

        List<ScheduleRecord> records = orders.stream()
                .map(order -> toRecord(order, itemsByOrder.getOrDefault(order.getId(), List.of())))
                .toList();
        int demand = orders.stream().map(BossOrder::getOrderNum).filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue).sum();
        return new Schedule(date, demand,
                new ScheduleStats(accepted, arrived, working, finished, settled), records);
    }

    private ScheduleDay scheduleDay(List<Long> owners, LocalDate date, String label) {
        int demand = ordersOn(owners, date).stream().map(BossOrder::getOrderNum)
                .filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).sum();
        return new ScheduleDay(date, label, demand);
    }

    private List<BossOrder> ordersOn(List<Long> owners, LocalDate date) {
        Date start = Date.from(date.atStartOfDay(SERVER_ZONE).toInstant());
        Date end = Date.from(date.plusDays(1).atStartOfDay(SERVER_ZONE).toInstant());
        return orderRepository.findByCreateByInAndOverlappingTime(owners, start, end).stream()
                .filter(order -> !BossStatus.ORDER_CANCELED.equals(order.getOrderStatus()))
                .filter(order -> !BossStatus.ORDER_DRAFT.equals(order.getOrderStatus()))
                .filter(order -> !BossStatus.ORDER_AUDIT_REJECT.equals(order.getOrderStatus()))
                .toList();
    }

    private List<Long> accountScope(Long bossId, Long accountId) {
        if (accountId != null) {
            BossRecruitAccount account = accountRepository.findByIdAndOwnerUserId(accountId, bossId)
                    .orElseThrow(() -> new ForbiddenBusinessException("招聘账号不属于当前老板"));
            return List.of(account.getTargetUserId() == null ? bossId : account.getTargetUserId());
        }
        List<Long> ids = accountRepository.findByOwnerUserIdOrderByIdAsc(bossId).stream()
                .map(a -> a.getTargetUserId() == null ? bossId : a.getTargetUserId()).filter(java.util.Objects::nonNull).distinct().toList();
        return ids.isEmpty() ? List.of(bossId) : ids;
    }

    private BossRecruitAccount resolveAccount(Long bossId, Long requestedId) {
        List<BossRecruitAccount> accounts = accountRepository.findByOwnerUserIdOrderByIdAsc(bossId);
        if (requestedId != null) {
            return accounts.stream().filter(a -> requestedId.equals(a.getId())).findFirst()
                    .orElseThrow(() -> new ForbiddenBusinessException("招聘账号不属于当前老板"));
        }
        if (accounts.isEmpty()) {
            BossRecruitAccount account = new BossRecruitAccount();
            account.setOwnerUserId(bossId);
            account.setName(userRepository.findById(bossId)
                    .map(u -> StringUtils.hasText(u.getRealName()) ? u.getRealName()
                            : StringUtils.hasText(u.getNickname()) ? u.getNickname() : "个人账号")
                    .orElse("个人账号"));
            account.setAuthorizationType("PERSONAL");
            account.setCurrent(true);
            return accountRepository.save(account);
        }
        BossRecruitAccount selected = accounts.stream().filter(a -> Boolean.TRUE.equals(a.getCurrent()))
                .findFirst().orElse(accounts.get(0));
        boolean changed = false;
        for (BossRecruitAccount account : accounts) {
            boolean current = account.getId().equals(selected.getId());
            if (!Boolean.valueOf(current).equals(account.getCurrent())) {
                account.setCurrent(current);
                changed = true;
            }
        }
        if (changed) accountRepository.saveAll(accounts);
        return selected;
    }

    private String resolveCity(Long bossId, String requestedCity) {
        if (StringUtils.hasText(requestedCity)) return requestedCity.trim();
        return userRepository.findById(bossId).map(u -> StringUtils.hasText(u.getCity()) ? u.getCity() : "")
                .orElse("");
    }

    private long count(Collection<BaseOrderItem> items, Collection<String> statuses) {
        return items.stream().filter(i -> statuses.contains(i.getStatus())).count();
    }

    private ScheduleRecord toRecord(BossOrder order, List<BaseOrderItem> items) {
        long accepted = count(items, List.of(BossStatus.ITEM_HIRED, BossStatus.ITEM_ON_WORK));
        String time = formatTime(order.getStartTime()) + "-" + formatTime(order.getEndTime());
        String sub = "招" + value(order.getOrderNum()) + "人 · " + time + " · 已接单" + accepted + "人";
        return new ScheduleRecord(order.getId(), text(order.getOrderTitle()), sub,
                displayStatus(order.getOrderStatus()), statusCode(order.getOrderStatus()), jobType(order));
    }

    private Account toAccount(BossRecruitAccount account, Long bossId) {
        String workCode = account.getWorkCode(), leaveCode = account.getLeaveCode();
        if (attendanceCodeService != null) { var state = attendanceCodeService.today(bossId); workCode = String.valueOf(state.get("workCode")); leaveCode = String.valueOf(state.get("leaveCode")); }
        return new Account(account.getId(), text(account.getName()), account.getAvatar(),
                text(account.getAuthorizationType()), text(workCode), text(leaveCode));
    }

    private String displayStatus(String status) {
        return BossStatus.ORDER_RECRUITING.equals(status) ? "进行中" : text(status);
    }

    private String statusCode(String status) {
        if (BossStatus.ORDER_RECRUITING.equals(status)) return "WORKING";
        if (BossStatus.ORDER_RECRUIT_END.equals(status)) return "RECRUIT_END";
        if (BossStatus.ORDER_PENDING_SETTLE.equals(status)) return "PENDING_SETTLEMENT";
        if (BossStatus.ORDER_COMPLETED.equals(status)) return "COMPLETED";
        if (BossStatus.ORDER_CANCELED.equals(status)) return "CANCELED";
        if (BossStatus.ORDER_PENDING_AUDIT.equals(status)) return "PENDING_AUDIT";
        return "UNKNOWN";
    }

    private String jobType(BossOrder order) {
        return order.getJobCategoryId() == null ? text(order.getType()).toUpperCase() : "INDUSTRY";
    }

    private String formatTime(Date date) {
        return date == null ? "--:--" : date.toInstant().atZone(SERVER_ZONE).toLocalTime().format(TIME);
    }

    private int value(Integer value) { return value == null ? 0 : value; }
    private String text(String value) { return value == null ? "" : value; }
}
