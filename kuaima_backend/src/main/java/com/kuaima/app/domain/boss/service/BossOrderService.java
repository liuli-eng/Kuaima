package com.kuaima.app.domain.boss.service;

import java.sql.Date;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.constant.BossType;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.model.BossOrderQuery;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.repository.BossOrderSpecifications;
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.user.service.CertificationService;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.security.model.LoginUser;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BossOrderService {

    private final BossOrderRespository orderRepository;
    private final BaseOrderItemRespository itemRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final SettlementRespository settlementRespository;
    private final CertificationService certificationService;

    @org.springframework.beans.factory.annotation.Autowired
    public BossOrderService(BossOrderRespository orderRepository,
                            BaseOrderItemRespository itemRepository,
                            UserRepository userRepository,
                            MessageService messageService,
                            SettlementRespository settlementRespository,
                            CertificationService certificationService) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.settlementRespository = settlementRespository;
        this.certificationService = certificationService;
    }

    public BossOrderService(BossOrderRespository orderRepository,
                            BaseOrderItemRespository itemRepository,
                            UserRepository userRepository,
                            MessageService messageService,
                            SettlementRespository settlementRespository) {
        this(orderRepository, itemRepository, userRepository, messageService,
                settlementRespository, null);
    }

    // ==================== 订单管理 ====================

    /** 发布招工订单，初始状态为"待审核"，admin 审核通过后变为"招工中" */
    @Transactional
    public BossOrder createOrder(BossOrder order) {
        if (!StringUtils.hasText(order.getOrderTitle())) {
            throw new IllegalArgumentException("订单标题不能为空");
        }
        if (!StringUtils.hasText(order.getType())) {
            throw new IllegalArgumentException("招工类型不能为空");
        }
        if (!BossType.isValid(order.getType())) {
            throw new IllegalArgumentException("招工类型不合法: " + order.getType());
        }
        if (!StringUtils.hasText(order.getPostion())) {
            throw new IllegalArgumentException("招聘岗位不能为空");
        }
        if (order.getOrderNum() == null || order.getOrderNum() <= 0) {
            throw new IllegalArgumentException("招工人数必须大于 0");
        }
        if (order.getDuration() == null || order.getDuration() <= 0) {
            throw new IllegalArgumentException("工作时长必须大于 0");
        }
        if (order.getSalary() == null || order.getSalary() <= 0) {
            throw new IllegalArgumentException("工资必须大于 0");
        }
        validateOrderCoordinates(order.getLongitude(), order.getLatitude());
        checkTimeRange(order.getStartTime(), order.getEndTime());
        applyRecruitSettings(order, order);
        // 试工时间仅月结类型有效
        if (!BossType.MONTH.equals(order.getType())) {
            order.setTrialDuration(null);
        }
        order.setOrderStatus(BossStatus.ORDER_PENDING_AUDIT);
        return orderRepository.save(order);
    }

    /** admin 审核通过：订单从"待审核"变为"招工中"，并发岗位广播 */
    @Transactional
    public BossOrder auditPass(Long id) {
        BossOrder order = getOrderOrThrow(id);
        if (!BossStatus.ORDER_PENDING_AUDIT.equals(order.getOrderStatus())) {
            throw new IllegalStateException("仅待审核的订单可以审核通过");
        }
        order.setOrderStatus(BossStatus.ORDER_RECRUITING);
        order.setAuditBy(currentAdminUsername());
        order.setAuditTime(new java.util.Date());
        BossOrder saved = orderRepository.save(order);
        // 审核通过后，广播给全部员工
        messageService.broadcastToUsers(MessageType.ORDER_PUBLISH, "新岗位发布",
                jobSummary(saved) + "，快来报名吧！", BizType.ORDER, saved.getId());
        return saved;
    }

    /** admin 审核拒绝 */
    @Transactional
    public BossOrder auditReject(Long id, String reason) {
        BossOrder order = getOrderOrThrow(id);
        if (!BossStatus.ORDER_PENDING_AUDIT.equals(order.getOrderStatus())) {
            throw new IllegalStateException("仅待审核的订单可以审核拒绝");
        }
        order.setOrderStatus(BossStatus.ORDER_AUDIT_REJECT);
        order.setAuditBy(currentAdminUsername());
        order.setAuditTime(new java.util.Date());
        order.setOrderRemark(StringUtils.hasText(reason) ? ("[审核拒绝] " + reason) : order.getOrderRemark());
        return orderRepository.save(order);
    }

    /** 获取当前登录管理员用户名 */
    private String currentAdminUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser u) {
            return u.username();
        }
        return "system";
    }

    /** 修改订单，仅"待审核"或"招工中"状态可修改；传入字段非空才会更新 */
    @Transactional
    public BossOrder updateOrder(Long id, BossOrder update) {
        BossOrder order = getOrderOrThrow(id);
        if (!BossStatus.ORDER_RECRUITING.equals(order.getOrderStatus())
                && !BossStatus.ORDER_PENDING_AUDIT.equals(order.getOrderStatus())) {
            throw new IllegalStateException("仅待审核或招工中的订单可以修改");
        }
        if (StringUtils.hasText(update.getOrderTitle())) {
            order.setOrderTitle(update.getOrderTitle());
        }
        if (StringUtils.hasText(update.getType())) {
            if (!BossType.isValid(update.getType())) {
                throw new IllegalArgumentException("招工类型不合法: " + update.getType());
            }
            order.setType(update.getType());
            // 改为非月结时清空试工时间
            if (!BossType.MONTH.equals(update.getType())) {
                order.setTrialDuration(null);
            }
        }
        if (StringUtils.hasText(update.getPostion())) {
            order.setPostion(update.getPostion());
        }
        if (StringUtils.hasText(update.getOrderContent())) {
            order.setOrderContent(update.getOrderContent());
        }
        if (StringUtils.hasText(update.getOrderRemark())) {
            order.setOrderRemark(update.getOrderRemark());
        }
        if (StringUtils.hasText(update.getAddress())) {
            order.setAddress(update.getAddress());
        }
        if (StringUtils.hasText(update.getTags())) {
            order.setTags(update.getTags());
        }
        applyFilterFields(order, update);
        applyRecruitSettings(order, update);
        if (update.getOrderNum() != null) {
            if (update.getOrderNum() <= 0) {
                throw new IllegalArgumentException("招工人数必须大于 0");
            }
            order.setOrderNum(update.getOrderNum());
        }
        if (update.getDuration() != null) {
            if (update.getDuration() <= 0) {
                throw new IllegalArgumentException("工作时长必须大于 0");
            }
            order.setDuration(update.getDuration());
        }
        if (update.getSalary() != null) {
            if (update.getSalary() <= 0) {
                throw new IllegalArgumentException("工资必须大于 0");
            }
            order.setSalary(update.getSalary());
        }
        if (update.getStartTime() != null) {
            order.setStartTime(update.getStartTime());
        }
        if (update.getEndTime() != null) {
            order.setEndTime(update.getEndTime());
        }
        // 试工时间：仅月结订单可设置
        if (BossType.MONTH.equals(order.getType()) && StringUtils.hasText(update.getTrialDuration())) {
            order.setTrialDuration(update.getTrialDuration());
        }
        validateOrderCoordinates(order.getLongitude(), order.getLatitude());
        checkTimeRange(order.getStartTime(), order.getEndTime());
        return orderRepository.save(order);
    }

    /** 订单详情 */
    public BossOrder getOrder(Long id) {
        BossOrder order = getOrderOrThrow(id);
        normalizeRecruitSettings(order);
        return order;
    }

    /**
     * 订单列表分页查询，可按类型/状态/标题过滤，按 id 倒序（最新在前）。
     * bossId 非空时仅返回该老板创建的岗位；为空时保留零工端公开列表的原有查询行为。
     */
    public Page<BossOrder> listOrders(Long bossId, BossOrderQuery query) {
        validateListQuery(query);
        // 页码从 0 开始，size 限制在 [1, 100]
        int safePage = Math.max(query.getPage(), 0);
        int safeSize = Math.min(Math.max(query.getSize(), 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<BossOrder> result = orderRepository.findAll(BossOrderSpecifications.from(bossId, query), pageable);
        fillCurrentApply(result.getContent());
        result.getContent().forEach(this::normalizeRecruitSettings);
        return result;
    }

    /** 兼容现有服务层调用。 */
    public Page<BossOrder> listOrders(Long bossId, String type, String status, String title, int page, int size) {
        BossOrderQuery query = new BossOrderQuery();
        query.setType(type);
        query.setStatus(status);
        query.setTitle(title);
        query.setPage(page);
        query.setSize(size);
        return listOrders(bossId, query);
    }

    /** 删除订单，仅"待审核"/"审核拒绝"/"招工中"/"取消招工"状态可删除 */
    @Transactional
    public void deleteOrder(Long id) {
        BossOrder order = getOrderOrThrow(id);
        String s = order.getOrderStatus();
        if (!BossStatus.ORDER_PENDING_AUDIT.equals(s)
                && !BossStatus.ORDER_AUDIT_REJECT.equals(s)
                && !BossStatus.ORDER_RECRUITING.equals(s)
                && !BossStatus.ORDER_CANCELED.equals(s)) {
            throw new IllegalStateException("该状态下的订单不可删除");
        }
        orderRepository.delete(order);
    }

    /**
     * 订单状态流转：
     * 招工中 -> 招工结束 -> 待结算 -> 已完成；任意未完成状态 -> 取消招工
     */
    @Transactional
    public BossOrder changeOrderStatus(Long id, String targetStatus) {
        BossOrder order = getOrderOrThrow(id);
        String current = order.getOrderStatus();

        // 兼容修复前已进入“待结算”但尚未生成结算单的历史订单；重复请求保持幂等并补齐结算单。
        if (BossStatus.ORDER_PENDING_SETTLE.equals(current)
                && BossStatus.ORDER_PENDING_SETTLE.equals(targetStatus)) {
            markHiredItemsOnWork(order);
            createPendingSettlements(order);
            return order;
        }

        boolean validTransition = switch (current) {
            case BossStatus.ORDER_RECRUITING ->
                    targetStatus.equals(BossStatus.ORDER_RECRUIT_END) || targetStatus.equals(BossStatus.ORDER_CANCELED);
            case BossStatus.ORDER_RECRUIT_END ->
                    targetStatus.equals(BossStatus.ORDER_PENDING_SETTLE) || targetStatus.equals(BossStatus.ORDER_CANCELED);
            case BossStatus.ORDER_PENDING_SETTLE ->
                    targetStatus.equals(BossStatus.ORDER_COMPLETED) || targetStatus.equals(BossStatus.ORDER_CANCELED);
            default -> false;
        };
        if (!validTransition) {
            throw new IllegalStateException("不允许从「" + current + "」流转到「" + targetStatus + "」");
        }

        order.setOrderStatus(targetStatus);

        // 取消招工：把该订单下所有未完成的报名记录置为"取消招工"，并收集受影响用户以便通知
        List<Long> canceledUserIds = new ArrayList<>();
        if (BossStatus.ORDER_CANCELED.equals(targetStatus)) {
            List<BaseOrderItem> items = itemRepository.findByOrderId(id);
            for (BaseOrderItem item : items) {
                if (!BossStatus.ITEM_FINISHED.equals(item.getStatus())) {
                    item.setStatus(BossStatus.ITEM_CANCEL_BY_BOSS);
                    item.setCancelDate(Date.valueOf(LocalDate.now()));
                    canceledUserIds.add(item.getUserId());
                }
            }
            itemRepository.saveAll(items);
        }
        BossOrder saved = orderRepository.save(order);
        if (BossStatus.ORDER_PENDING_SETTLE.equals(targetStatus)) {
            // 老板在订单页“确认全部到岗”后，已录用记录同步进入已到岗，再生成待支付结算单。
            markHiredItemsOnWork(saved);
            createPendingSettlements(saved);
        }
        // 取消招工：通知该订单所有受影响报名者
        if (!canceledUserIds.isEmpty()) {
            messageService.sendToList(canceledUserIds, UserRole.USER, MessageType.ORDER_CANCEL, "招工已取消",
                    "很抱歉，您报名的「" + order.getOrderTitle() + "」岗位已取消招工。",
                    BizType.ORDER, order.getId());
        }
        return saved;
    }

    /**
     * 【预留】岗位快开始提醒：手动/前端触发，通知该订单 已报名/已录用/已到岗 的用户。
     * 后续如需自动提醒，可在此之上加定时任务扫描 startTime。
     */
    @Transactional
    public void notifyOrderStart(Long orderId) {
        BossOrder order = getOrderOrThrow(orderId);
        List<Long> userIds = itemRepository.findByOrderId(orderId).stream()
                .filter(i -> BossStatus.ITEM_APPLIED.equals(i.getStatus())
                        || BossStatus.ITEM_HIRED.equals(i.getStatus())
                        || BossStatus.ITEM_ON_WORK.equals(i.getStatus()))
                .map(BaseOrderItem::getUserId)
                .distinct()
                .toList();
        messageService.sendToList(userIds, UserRole.USER, MessageType.ORDER_START_REMIND, "岗位即将开始",
                "您报名的「" + order.getOrderTitle() + "」" + order.getPostion() + "岗位即将开始，请提前做好准备准时到岗！",
                BizType.ORDER, order.getId());
    }

    // ==================== 报名管理 ====================

    /** 用户报名订单，订单须"招工中"且未重复报名；月结订单可勾选"我要试工" */
    @Transactional
    public BaseOrderItem applyOrder(Long orderId, Long userId, String remark, Boolean trial) {
        BossOrder order = getOrderOrThrow(orderId);
        if (!BossStatus.ORDER_RECRUITING.equals(order.getOrderStatus())) {
            throw new IllegalStateException("该订单当前不可报名");
        }
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在: " + userId);
        }
        if (itemRepository.existsByOrderIdAndUserId(orderId, userId)) {
            throw new IllegalStateException("您已报名过该订单");
        }
        // 报名人数校验：已录用 + 已到岗 + 已完成的人数不能超过招工人数
        long hiredCount = itemRepository.findByOrderId(orderId).stream()
                .filter(i -> BossStatus.ITEM_HIRED.equals(i.getStatus())
                        || BossStatus.ITEM_ON_WORK.equals(i.getStatus())
                        || BossStatus.ITEM_FINISHED.equals(i.getStatus()))
                .count();
        if (hiredCount >= order.getOrderNum()) {
            throw new IllegalStateException("该订单已招满");
        }
        // 试工标记：仅月结订单可勾选
        boolean wantTrial = Boolean.TRUE.equals(trial);
        if (wantTrial && !BossType.MONTH.equals(order.getType())) {
            throw new IllegalStateException("仅月结订单可选择试工");
        }

        BaseOrderItem item = new BaseOrderItem();
        item.setOrderId(orderId);
        item.setUserId(userId);
        // 统一走审核流程：抢单后一律为「已报名」，审核通过后才变为「已录用」
        item.setStatus(BossStatus.ITEM_APPLIED);
        item.setRemark(remark);
        item.setTrialRequested(wantTrial);
        item.setApplyDate(Date.valueOf(LocalDate.now()));
        BaseOrderItem saved = itemRepository.save(item);
        // 通知老板有新的报名待审核
        String content = userName(userId) + " 报名了您发布的「" + order.getOrderTitle() + "」"
                + order.getPostion() + "岗位，请及时审核报名信息";
        messageService.sendToUser(order.getCreateBy(), UserRole.BOSS, MessageType.ORDER_APPLY, "有新的报名待审核",
                content, BizType.ITEM, saved.getId());
        return saved;
    }

    /** 老板录用：已报名 -> 已录用 */
    @Transactional
    public BaseOrderItem hireItem(Long itemId) {
        BaseOrderItem item = getItemOrThrow(itemId);
        if (!BossStatus.ITEM_APPLIED.equals(item.getStatus())) {
            throw new IllegalStateException("仅已报名的记录可以录用");
        }
        item.setStatus(BossStatus.ITEM_HIRED);
        item.setHireDate(Date.valueOf(LocalDate.now()));
        BaseOrderItem saved = itemRepository.save(item);
        // 录用结果：通知被录用的零工
        BossOrder order = getOrderOrThrow(item.getOrderId());
        // 已录用/已到岗/已完成人数达到需求人数后，自动结束招工，停止继续报名。
        if (BossStatus.ORDER_RECRUITING.equals(order.getOrderStatus())
                && order.getOrderNum() != null && order.getOrderNum() > 0) {
            long hiredCount = itemRepository.findByOrderId(order.getId()).stream()
                    .filter(i -> BossStatus.ITEM_HIRED.equals(i.getStatus())
                            || BossStatus.ITEM_ON_WORK.equals(i.getStatus())
                            || BossStatus.ITEM_FINISHED.equals(i.getStatus()))
                    .count();
            if (hiredCount >= order.getOrderNum()) {
                order.setOrderStatus(BossStatus.ORDER_RECRUIT_END);
                orderRepository.save(order);
            }
        }
        messageService.sendToUser(item.getUserId(), UserRole.USER, MessageType.ORDER_HIRE, "录用通知",
                "恭喜您！您已被「" + order.getOrderTitle() + "」" + order.getPostion() + "岗位录用（"
                        + order.getSalary() + "元/天），请准时到岗。",
                BizType.ITEM, item.getId());
        return saved;
    }

    /** 老板拒绝报名：仅待审核的“已报名”记录可操作。 */
    @Transactional
    public BaseOrderItem rejectItem(Long itemId, Long bossId, String reason) {
        BaseOrderItem item = getItemOrThrow(itemId);
        BossOrder order = getOrderOrThrow(item.getOrderId());
        if (bossId == null || !bossId.equals(order.getCreateBy())) {
            throw new ForbiddenBusinessException("无权处理该岗位的报名记录");
        }
        if (!BossStatus.ITEM_APPLIED.equals(item.getStatus())) {
            throw new IllegalStateException("仅待审核的报名记录可以拒绝");
        }
        item.setStatus(BossStatus.ITEM_REJECTED);
        item.setCancelReason(StringUtils.hasText(reason) ? reason.trim() : null);
        item.setCancelDate(Date.valueOf(LocalDate.now()));
        BaseOrderItem saved = itemRepository.save(item);
        messageService.sendToUser(item.getUserId(), UserRole.USER, MessageType.ORDER_APPLY_REJECT, "报名审核未通过",
                "您报名的「" + order.getOrderTitle() + "」岗位未通过审核"
                        + (StringUtils.hasText(reason) ? "，原因：" + reason.trim() : ""),
                BizType.ITEM, item.getId());
        return saved;
    }

    /** 用户确认到岗：已录用 -> 已到岗 */
    @Transactional
    public BaseOrderItem confirmWork(Long itemId) {
        BaseOrderItem item = getItemOrThrow(itemId);
        if (!BossStatus.ITEM_HIRED.equals(item.getStatus())) {
            throw new IllegalStateException("仅已录用的记录可以确认到岗");
        }
        item.setStatus(BossStatus.ITEM_ON_WORK);
        item.setWorkDate(Date.valueOf(LocalDate.now()));
        BaseOrderItem saved = itemRepository.save(item);
        // 零工确认到岗：通知老板
        BossOrder order = getOrderOrThrow(item.getOrderId());
        // 招工已结束且所有已录取零工均已确认到岗后，订单进入待结算。
        if (BossStatus.ORDER_RECRUIT_END.equals(order.getOrderStatus())) {
            List<BaseOrderItem> items = itemRepository.findByOrderId(order.getId());
            List<BaseOrderItem> activeHired = items.stream()
                    .filter(i -> BossStatus.ITEM_HIRED.equals(i.getStatus())
                            || BossStatus.ITEM_ON_WORK.equals(i.getStatus())
                            || BossStatus.ITEM_FINISHED.equals(i.getStatus()))
                    .toList();
            boolean allArrived = !activeHired.isEmpty()
                    && activeHired.stream().allMatch(i ->
                            BossStatus.ITEM_ON_WORK.equals(i.getStatus())
                                    || BossStatus.ITEM_FINISHED.equals(i.getStatus()));
            if (allArrived) {
                order.setOrderStatus(BossStatus.ORDER_PENDING_SETTLE);
                orderRepository.save(order);
                createPendingSettlements(order);
            }
        }
        messageService.sendToUser(order.getCreateBy(), UserRole.BOSS, MessageType.ITEM_WORK_CONFIRM, "零工已到岗",
                userName(item.getUserId()) + " 已到达「" + order.getOrderTitle() + "」岗位，请留意安排工作。",
                BizType.ITEM, item.getId());
        return saved;
    }

    /** 完成：已到岗 -> 已完成 */
    @Transactional
    public BaseOrderItem finishItem(Long itemId) {
        BaseOrderItem item = getItemOrThrow(itemId);
        if (!BossStatus.ITEM_ON_WORK.equals(item.getStatus())) {
            throw new IllegalStateException("仅已到岗的记录可以完成");
        }
        item.setStatus(BossStatus.ITEM_FINISHED);
        item.setFinishDate(Date.valueOf(LocalDate.now()));
        BaseOrderItem saved = itemRepository.save(item);
        BossOrder order = getOrderOrThrow(item.getOrderId());
        if (BossStatus.ORDER_PENDING_SETTLE.equals(order.getOrderStatus())) {
            createPendingSettlements(order);
        }
        return saved;
    }

    /** 用户取消报名：已报名/已录用 -> 取消报名 */
    @Transactional
    public BaseOrderItem cancelItem(Long itemId, String reason) {
        BaseOrderItem item = getItemOrThrow(itemId);
        if (!BossStatus.ITEM_APPLIED.equals(item.getStatus())
                && !BossStatus.ITEM_HIRED.equals(item.getStatus())) {
            throw new IllegalStateException("当前状态不可取消报名");
        }
        item.setStatus(BossStatus.ITEM_CANCELED);
        item.setCancelReason(reason);
        item.setCancelDate(Date.valueOf(LocalDate.now()));
        BaseOrderItem saved = itemRepository.save(item);
        // 零工取消报名：通知老板
        BossOrder order = getOrderOrThrow(item.getOrderId());
        messageService.sendToUser(order.getCreateBy(), UserRole.BOSS, MessageType.ITEM_CANCEL, "报名取消提醒",
                userName(item.getUserId()) + " 取消了「" + order.getOrderTitle() + "」岗位的报名"
                        + (StringUtils.hasText(reason) ? "，原因：" + reason : ""),
                BizType.ITEM, item.getId());
        return saved;
    }

    /** 某订单的报名列表 */
    public List<BaseOrderItem> listItemsByOrder(Long orderId) {
        return itemRepository.findByOrderId(orderId);
    }

    /** 某用户报名的记录 */
    public List<BaseOrderItem> listItemsByUser(Long userId) {
        return itemRepository.findByUserId(userId);
    }

    // ==================== 草稿管理 ====================

    /** 保存草稿：不校验必填字段，状态固定为"草稿" */
    @Transactional
    public BossOrder saveDraft(BossOrder order) {
        validateOrderCoordinates(order.getLongitude(), order.getLatitude());
        order.setOrderStatus(BossStatus.ORDER_DRAFT);
        return orderRepository.save(order);
    }

    /** 某老板的草稿列表 */
    public List<BossOrder> listDrafts(Long userId) {
        return orderRepository.findByOrderStatusAndCreateByOrderByIdDesc(BossStatus.ORDER_DRAFT, userId);
    }

    /** 更新草稿：仅草稿状态可更新 */
    @Transactional
    public BossOrder updateDraft(Long id, BossOrder update) {
        BossOrder order = getOrderOrThrow(id);
        if (!BossStatus.ORDER_DRAFT.equals(order.getOrderStatus())) {
            throw new IllegalStateException("仅草稿状态的订单可以更新");
        }
        if (StringUtils.hasText(update.getOrderTitle())) order.setOrderTitle(update.getOrderTitle());
        if (StringUtils.hasText(update.getType())) order.setType(update.getType());
        if (StringUtils.hasText(update.getPostion())) order.setPostion(update.getPostion());
        if (update.getOrderNum() != null) order.setOrderNum(update.getOrderNum());
        if (update.getDuration() != null) order.setDuration(update.getDuration());
        if (update.getSalary() != null) order.setSalary(update.getSalary());
        if (StringUtils.hasText(update.getOrderContent())) order.setOrderContent(update.getOrderContent());
        if (StringUtils.hasText(update.getOrderRemark())) order.setOrderRemark(update.getOrderRemark());
        if (StringUtils.hasText(update.getAddress())) order.setAddress(update.getAddress());
        if (StringUtils.hasText(update.getTags())) order.setTags(update.getTags());
        applyFilterFields(order, update);
        applyRecruitSettings(order, update);
        if (update.getStartTime() != null) order.setStartTime(update.getStartTime());
        if (update.getEndTime() != null) order.setEndTime(update.getEndTime());
        if (StringUtils.hasText(update.getTrialDuration())) order.setTrialDuration(update.getTrialDuration());
        validateOrderCoordinates(order.getLongitude(), order.getLatitude());
        return orderRepository.save(order);
    }

    // ==================== 统计与资料 ====================

    /** 老板首页统计：总订单数、招工中数、报名数、已结算金额(元) */
    public java.util.Map<String, Object> getBossStats(Long userId) {
        List<BossOrder> orders = orderRepository.findByCreateByOrderByIdDesc(userId);
        int totalOrders = orders.size();
        long recruitingCount = orders.stream().filter(o -> BossStatus.ORDER_RECRUITING.equals(o.getOrderStatus())).count();
        long applicantCount = itemRepository.countApplicantsByBossId(userId);
        // 已结算金额：该老板所有已支付结算单 totalAmount 之和(分→元)
        long totalSpentCent = 0;
        for (BossOrder o : orders) {
            List<Settlement> settlements = settlementRespository.findByOrderIdOrderByIdDesc(o.getId());
            totalSpentCent += settlements.stream()
                    .filter(s -> "已支付".equals(s.getStatus()))
                    .mapToLong(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0)
                    .sum();
        }
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("recruitingCount", recruitingCount);
        stats.put("applicantCount", applicantCount);
        stats.put("settledAmount", totalSpentCent / 100.0);
        return stats;
    }

    /** 老板账户统计：与首页统计类似，用于"我的"页面 */
    public java.util.Map<String, Object> getBossProfileStats(Long userId) {
        return getBossStats(userId);
    }

    /** 老板资料 */
    public User getBossProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
    }

    // ==================== 高级筛选 ====================

    /** 零工端高级筛选岗位（仅招工中） */
    public Page<BossOrder> filterOrders(String city, Integer salaryMin, Integer salaryMax,
                                        String tag, String type, Integer duration, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        return orderRepository.filter(city, salaryMin, salaryMax, tag, type, duration, pageable);
    }

    // ==================== 月结/压薪订单 ====================

    /** 零工的月结订单列表 */
    public List<BaseOrderItem> listMonthlyOrders(Long userId) {
        return itemRepository.findByUserIdAndOrderType(userId, BossType.MONTH);
    }

    /** 零工的压薪日结订单列表 */
    public List<BaseOrderItem> listPressSalaryOrders(Long userId) {
        return itemRepository.findByUserIdAndOrderType(userId, BossType.HELD_BACK);
    }

    // ==================== 企业认证 ====================

    /** 提交企业认证 */
    @Transactional
    public User submitEnterpriseCert(Long userId, String companyName, String industry, String licenseNo, String legalRep) {
        if (certificationService == null) {
            throw new IllegalStateException("认证服务未初始化");
        }
        return certificationService.submitEnterprise(userId, companyName, industry, licenseNo, legalRep);
    }

    // ==================== 内部方法 ====================

    private BossOrder getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在: " + id));
    }

    private BaseOrderItem getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("报名记录不存在: " + id));
    }

    /** 订单进入待结算时，为已到岗/已完成零工生成待支付结算单。 */
    private void createPendingSettlements(BossOrder order) {
        if (order.getSalary() == null || order.getSalary() <= 0) {
            throw new IllegalStateException("订单工资未设置，无法生成结算单");
        }
        for (BaseOrderItem item : itemRepository.findByOrderId(order.getId())) {
            if (!BossStatus.ITEM_ON_WORK.equals(item.getStatus())
                    && !BossStatus.ITEM_FINISHED.equals(item.getStatus())) {
                continue;
            }
            if (settlementRespository.existsByItemIdAndStatusIn(item.getId(),
                    List.of(SettlementStatus.PENDING, SettlementStatus.PAID))) {
                continue;
            }
            int workDays = resolveSettlementWorkDays(item);
            long wage = order.getSalary() * (long) workDays * 100L;
            Settlement settlement = new Settlement();
            settlement.setItemId(item.getId());
            settlement.setOrderId(order.getId());
            settlement.setWorkerId(item.getUserId());
            settlement.setWorkDays(workDays);
            settlement.setWage(wage);
            settlement.setServiceFee(0L);
            settlement.setTotalAmount(wage);
            settlement.setStatus(SettlementStatus.PENDING);
            settlementRespository.save(settlement);
        }
    }

    private void markHiredItemsOnWork(BossOrder order) {
        List<BaseOrderItem> hiredItems = itemRepository.findByOrderId(order.getId()).stream()
                .filter(item -> BossStatus.ITEM_HIRED.equals(item.getStatus()))
                .toList();
        if (hiredItems.isEmpty()) {
            return;
        }
        Date workDate = Date.valueOf(LocalDate.now());
        hiredItems.forEach(item -> {
            item.setStatus(BossStatus.ITEM_ON_WORK);
            if (item.getWorkDate() == null) {
                item.setWorkDate(workDate);
            }
        });
        itemRepository.saveAll(hiredItems);
    }

    private int resolveSettlementWorkDays(BaseOrderItem item) {
        if (item.getWorkDate() != null && item.getFinishDate() != null
                && !item.getFinishDate().before(item.getWorkDate())) {
            long days = ChronoUnit.DAYS.between(
                    item.getWorkDate().toLocalDate(), item.getFinishDate().toLocalDate()) + 1;
            return Math.max(1, (int) days);
        }
        return 1;
    }

    /** 为岗位列表批量填充有效报名数（排除取消报名、取消招工）。 */
    private void fillCurrentApply(List<BossOrder> orders) {
        if (orders.isEmpty()) {
            return;
        }
        List<Long> orderIds = orders.stream().map(BossOrder::getId).toList();
        List<String> activeStatuses = List.of(
                BossStatus.ITEM_APPLIED,
                BossStatus.ITEM_HIRED,
                BossStatus.ITEM_ON_WORK,
                BossStatus.ITEM_FINISHED);
        Map<Long, Long> countByOrderId = itemRepository
                .countByOrderIdsAndStatuses(orderIds, activeStatuses)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()));
        orders.forEach(order -> order.setCurrentApply(countByOrderId.getOrDefault(order.getId(), 0L)));
    }

    /** 校验开始/结束时间：两者都填时开始必须早于结束 */
    private void checkTimeRange(java.util.Date startTime, java.util.Date endTime) {
        if (startTime != null && endTime != null && startTime.after(endTime)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
    }

    /** 更新筛选相关字段；null/空字符串表示调用方未修改该字段。 */
    private void applyFilterFields(BossOrder target, BossOrder source) {
        if (source.getJobCategoryId() != null) {
            target.setJobCategoryId(source.getJobCategoryId());
        }
        if (source.getLongitude() != null) {
            target.setLongitude(source.getLongitude());
        }
        if (source.getLatitude() != null) {
            target.setLatitude(source.getLatitude());
        }
        if (StringUtils.hasText(source.getExperience())) {
            target.setExperience(source.getExperience().trim());
        }
        if (StringUtils.hasText(source.getGender())) {
            target.setGender(source.getGender().trim());
        }
    }

    /** 创建时写入默认值，更新时仅覆盖请求中明确传入的设置。 */
    private void applyRecruitSettings(BossOrder target, BossOrder source) {
        if (source == target || source.getSignMode() != null || target.getSignMode() == null) {
            if (StringUtils.hasText(source.getSignMode())) {
                target.setSignMode(source.getSignMode().trim());
            } else if (target.getSignMode() == null) {
                target.setSignMode("auto");
            }
        }
        if (source == target || source.getPhoneNotify() != null || target.getPhoneNotify() == null) {
            target.setPhoneNotify(source.getPhoneNotify() != null ? source.getPhoneNotify() : Boolean.TRUE);
        }
        if (source == target || source.getSignNotify() != null || target.getSignNotify() == null) {
            target.setSignNotify(source.getSignNotify() != null ? source.getSignNotify() : Boolean.TRUE);
        }
        if (source == target || source.getStartRemind() != null || target.getStartRemind() == null) {
            target.setStartRemind(source.getStartRemind() != null ? source.getStartRemind() : Boolean.TRUE);
        }
        if (source == target || source.getSettleNotify() != null || target.getSettleNotify() == null) {
            target.setSettleNotify(source.getSettleNotify() != null ? source.getSettleNotify() : Boolean.TRUE);
        }
        validateSignMode(target.getSignMode());
    }

    private void normalizeRecruitSettings(BossOrder order) {
        if (!StringUtils.hasText(order.getSignMode())) {
            order.setSignMode("auto");
        }
        if (order.getPhoneNotify() == null) order.setPhoneNotify(Boolean.TRUE);
        if (order.getSignNotify() == null) order.setSignNotify(Boolean.TRUE);
        if (order.getStartRemind() == null) order.setStartRemind(Boolean.TRUE);
        if (order.getSettleNotify() == null) order.setSettleNotify(Boolean.TRUE);
    }

    private void validateSignMode(String signMode) {
        if (!"auto".equals(signMode) && !"manual".equals(signMode)) {
            throw new IllegalArgumentException("报名方式只能是 auto 或 manual");
        }
    }

    /** 坐标必须成对出现并落在经纬度合法范围内。历史订单可保持坐标为空。 */
    private void validateOrderCoordinates(BigDecimal longitude, BigDecimal latitude) {
        if ((longitude == null) != (latitude == null)) {
            throw new IllegalArgumentException("经度和纬度必须同时填写");
        }
        if (longitude != null && (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new IllegalArgumentException("经度必须在 -180 到 180 之间");
        }
        if (latitude != null && (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new IllegalArgumentException("纬度必须在 -90 到 90 之间");
        }
    }

    /** 校验列表筛选参数，避免生成无意义或不安全的距离/范围条件。 */
    private void validateListQuery(BossOrderQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }
        if (query.getSalaryMin() != null && query.getSalaryMax() != null
                && query.getSalaryMin() > query.getSalaryMax()) {
            throw new IllegalArgumentException("最低工资不能高于最高工资");
        }
        if (query.getSalaryMin() != null && query.getSalaryMin() < 0
                || query.getSalaryMax() != null && query.getSalaryMax() < 0) {
            throw new IllegalArgumentException("工资筛选值不能小于 0");
        }
        if (query.getStartDate() != null && query.getEndDate() != null
                && query.getStartDate().isAfter(query.getEndDate())) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        boolean hasLongitude = query.getLongitude() != null;
        boolean hasLatitude = query.getLatitude() != null;
        if (hasLongitude != hasLatitude) {
            throw new IllegalArgumentException("经度和纬度必须同时填写");
        }
        if (hasLongitude) {
            validateOrderCoordinates(query.getLongitude(), query.getLatitude());
        }
        if (query.getDistanceKm() != null && query.getDistanceKm() <= 0) {
            throw new IllegalArgumentException("距离必须大于 0 公里");
        }
        if (query.getDistanceKm() != null && !hasLongitude) {
            throw new IllegalArgumentException("按距离筛选时必须提供经度和纬度");
        }
        if (StringUtils.hasText(query.getTagMode())
                && !"ALL".equalsIgnoreCase(query.getTagMode().trim())
                && !"ANY".equalsIgnoreCase(query.getTagMode().trim())) {
            throw new IllegalArgumentException("tagMode 只能是 ALL 或 ANY");
        }
    }

    /** 用户显示名：优先昵称，其次 零工#id */
    private String userName(Long userId) {
        if (userId == null) {
            return "";
        }
        return userRepository.findById(userId)
                .map(User::getNickname)
                .filter(StringUtils::hasText)
                .orElse("零工#" + userId);
    }

    /** 岗位一句话摘要（广播/通知文案用） */
    private String jobSummary(BossOrder order) {
        StringBuilder sb = new StringBuilder("「")
                .append(order.getOrderTitle()).append("」招")
                .append(order.getPostion()).append(" ")
                .append(order.getOrderNum()).append("人，")
                .append(order.getSalary()).append("元/天");
        if (StringUtils.hasText(order.getAddress())) {
            sb.append("，地点：").append(order.getAddress());
        }
        return sb.toString();
    }
}
