package com.kuaima.app.domain.user.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.model.WorkerProfileModels.UpdateWorkerProfileRequest;
import com.kuaima.app.domain.user.model.WorkerProfileModels.WorkerProfile;
import com.kuaima.app.domain.user.model.WorkerOrderModels.WorkerOrder;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class WorkerProfileService {

    private final UserRepository userRepository;
    private final BaseOrderItemRespository itemRepository;
    private final BossOrderRespository orderRepository;

    public WorkerProfileService(UserRepository userRepository,
                                BaseOrderItemRespository itemRepository,
                                BossOrderRespository orderRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public WorkerProfile getProfile(Long userId) {
        return toProfile(getUserOrThrow(userId));
    }

    @Transactional
    public WorkerProfile updateProfile(Long userId, UpdateWorkerProfileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        validate(request);
        User user = getUserOrThrow(userId);
        if (request.avatar() != null) user.setAvatar(trimToNull(request.avatar()));
        if (request.nickname() != null) user.setNickname(trimToNull(request.nickname()));
        if (request.gender() != null) user.setGender(trimToNull(request.gender()));
        if (request.birthday() != null) user.setBirthday(request.birthday());
        if (request.city() != null) user.setCity(trimToNull(request.city()));
        if (request.skills() != null) user.setSkills(trimToNull(request.skills()));
        if (request.workYears() != null) user.setWorkYears(request.workYears());
        if (request.acceptNightShift() != null) user.setAcceptNightShift(request.acceptNightShift());
        if (request.introduction() != null) user.setIntroduction(trimToNull(request.introduction()));
        return toProfile(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<WorkerOrder> listOrders(Long userId, String type, String status, Pageable pageable) {
        if (type != null && !type.isBlank()
                && !"daily".equals(type) && !"heldBack".equals(type) && !"month".equals(type)) {
            throw new IllegalArgumentException("订单类型不合法: " + type);
        }
        Page<BaseOrderItem> items = itemRepository.findWorkerOrders(userId,
                blankToNull(type), blankToNull(status), pageable);
        if (items.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, items.getTotalElements());
        }
        Map<Long, BossOrder> orders = orderRepository.findAllById(
                        items.getContent().stream().map(BaseOrderItem::getOrderId).distinct().toList())
                .stream().collect(Collectors.toMap(BossOrder::getId, Function.identity()));
        Map<Long, User> bosses = userRepository.findAllById(orders.values().stream()
                        .map(BossOrder::getCreateBy).filter(id -> id != null).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return items.map(item -> toWorkerOrder(item, orders.get(item.getOrderId()), bosses));
    }

    private WorkerOrder toWorkerOrder(BaseOrderItem item, BossOrder order, Map<Long, User> bosses) {
        if (order == null) {
            throw new EntityNotFoundException("报名关联的订单不存在: " + item.getOrderId());
        }
        User boss = order.getCreateBy() == null ? null : bosses.get(order.getCreateBy());
        return new WorkerOrder(item.getId(), item.getOrderId(), item.getStatus(), item.getTrialRequested(),
                item.getApplyDate(), item.getHireDate(), item.getWorkDate(), item.getFinishDate(),
                order.getOrderTitle(), order.getType(), order.getSalary(), order.getAddress(),
                order.getStartTime(), order.getEndTime(), boss == null ? null : boss.getCompanyName(),
                order.getPostion(), order.getOrderStatus(), order.getDuration(), order.getTags());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validate(UpdateWorkerProfileRequest request) {
        if (request.nickname() != null && request.nickname().trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (request.birthday() != null && request.birthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("出生日期不能晚于今天");
        }
        if (request.workYears() != null && (request.workYears() < 0 || request.workYears() > 80)) {
            throw new IllegalArgumentException("工作年限必须在0到80年之间");
        }
    }

    private WorkerProfile toProfile(User user) {
        return new WorkerProfile(
                user.getId(), user.getAvatar(), user.getNickname(), user.getPhone(), user.getGender(),
                user.getBirthday(), user.getCity(), user.getSkills(), user.getWorkYears(),
                user.getAcceptNightShift(), user.getIntroduction());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
    }

    private String trimToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
