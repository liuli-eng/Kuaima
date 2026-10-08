package com.kuaima.app.domain.payroll.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.payroll.constant.PayrollConstants;
import com.kuaima.app.domain.payroll.entity.PayrollDetail;
import com.kuaima.app.domain.payroll.entity.PayrollOrder;
import com.kuaima.app.domain.payroll.repository.PayrollDetailRepository;
import com.kuaima.app.domain.payroll.repository.PayrollOrderRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PayrollService {

    private final PayrollOrderRepository orderRepository;
    private final PayrollDetailRepository detailRepository;

    public PayrollService(PayrollOrderRepository orderRepository, PayrollDetailRepository detailRepository) {
        this.orderRepository = orderRepository;
        this.detailRepository = detailRepository;
    }

    public Page<PayrollOrder> listOrders(String tab, String status, Long projectId, String keyword, Pageable pageable) {
        Specification<PayrollOrder> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if ("reviewed".equals(tab)) {
                predicates.add(cb.in(root.get("status")).value(PayrollConstants.ORDER_APPROVED).value(PayrollConstants.ORDER_REJECTED));
            } else {
                // submitted：除已撤回外都展示
                predicates.add(cb.notEqual(root.get("status"), PayrollConstants.ORDER_WITHDRAWN));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.get("projectId"), projectId));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("creator"), like),
                        cb.like(root.get("reviewBy"), like)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return orderRepository.findAll(spec, pageable);
    }

    public PayrollOrder getOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("发薪单不存在: " + id));
    }

    public List<PayrollDetail> details(Long id) {
        return detailRepository.findByPayrollId(id);
    }

    /** 创建发薪单并写入明细，自动汇总应发金额与人数。金额单位统一为「分」。 */
    @Transactional
    public PayrollOrder createOrder(PayrollOrder order, List<PayrollDetail> details, Long creatorId, String creator) {
        if (order.getStatus() == null) {
            order.setStatus(PayrollConstants.ORDER_PENDING);
        }
        // 管理后台创建薪单即视为已提交（待审批），submitTime 若未显式传入则默认为当前时间
        if (order.getSubmitTime() == null) {
            order.setSubmitTime(new java.util.Date());
        }
        if (order.getOrderNo() == null) {
            order.setOrderNo("TR" + System.currentTimeMillis());
        }
        if (creator != null) {
            order.setCreator(creator);
        }
        if (creatorId != null) {
            order.setCreatorId(creatorId);
        }
        order.setAmount(0L);
        order.setPeopleCount(0);
        PayrollOrder saved = orderRepository.save(order);

        long totalAmount = 0L;
        if (details != null) {
            for (PayrollDetail d : details) {
                d.setPayrollId(saved.getId());
                if (d.getStatus() == null) {
                    d.setStatus(PayrollConstants.DETAIL_PENDING);
                }
                // 应发金额 = 日薪（分）× 出勤天数
                // 只要日薪或出勤天数有值，就以日薪×天数为准（覆盖前端显式传的 amount）
                long dailyWage = d.getDailyWage() == null ? 0L : d.getDailyWage();
                int attendDays = d.getAttendDays() == null ? 0 : d.getAttendDays();
                if (dailyWage > 0 || attendDays > 0) {
                    d.setAmount(dailyWage * attendDays);
                } else if (d.getAmount() == null) {
                    d.setAmount(0L);
                }
                totalAmount += d.getAmount() == null ? 0L : d.getAmount();
                detailRepository.save(d);
            }
        }
        saved.setAmount(totalAmount);
        saved.setPeopleCount(details == null ? 0 : details.size());
        return orderRepository.save(saved);
    }

    @Transactional
    public PayrollOrder submit(Long id) {
        PayrollOrder order = getOrThrow(id);
        order.setSubmitTime(new java.util.Date());
        order.setStatus(PayrollConstants.ORDER_PENDING);
        return orderRepository.save(order);
    }

    @Transactional
    public PayrollOrder approve(Long id, String reviewer) {
        PayrollOrder order = getOrThrow(id);
        order.setStatus(PayrollConstants.ORDER_APPROVED);
        order.setReviewBy(reviewer);
        order.setReviewTime(new java.util.Date());
        // 明细保持 DETAIL_PENDING（待转账），等待打款完成后才置为 success
        return orderRepository.save(order);
    }

    @Transactional
    public PayrollOrder reject(Long id, String reviewer) {
        PayrollOrder order = getOrThrow(id);
        order.setStatus(PayrollConstants.ORDER_REJECTED);
        order.setReviewBy(reviewer);
        order.setReviewTime(new java.util.Date());
        return orderRepository.save(order);
    }

    @Transactional
    public PayrollOrder withdraw(Long id) {
        PayrollOrder order = getOrThrow(id);
        order.setStatus(PayrollConstants.ORDER_WITHDRAWN);
        return orderRepository.save(order);
    }

    /** 发薪管理统计：项目总数 / 本月发薪总额 / 本月发薪笔数 / 待我审批。 */
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<PayrollOrder> all = orderRepository.findAll();
        // 后台建单只填项目名、不传 projectId，若仅按 projectId 去重会恒为 0
        long projectTotal = all.stream()
                .map(PayrollService::projectKey)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
        LocalDate now = LocalDate.now();
        long monthAmount = all.stream()
                .filter(o -> PayrollConstants.ORDER_APPROVED.equals(o.getStatus()) && isThisMonth(o.getSubmitTime(), now))
                .mapToLong(o -> o.getAmount() == null ? 0L : o.getAmount()).sum();
        long monthCount = all.stream()
                .filter(o -> PayrollConstants.ORDER_APPROVED.equals(o.getStatus()) && isThisMonth(o.getSubmitTime(), now))
                .count();
        long pendingCount = all.stream().filter(o -> PayrollConstants.ORDER_PENDING.equals(o.getStatus())).count();

        result.put("projectTotal", projectTotal);
        result.put("monthAmount", monthAmount);
        result.put("monthCount", monthCount);
        result.put("pendingCount", pendingCount);
        return result;
    }

    /** 项目去重键：优先用 projectId，缺省回退 projectName。两者都为空表示未关联项目。 */
    private static String projectKey(PayrollOrder order) {
        if (order.getProjectId() != null) {
            return "id:" + order.getProjectId();
        }
        String name = order.getProjectName();
        return (name == null || name.isBlank()) ? null : "name:" + name.trim();
    }

    private boolean isThisMonth(java.util.Date date, LocalDate now) {
        if (date == null) {
            return false;
        }
        LocalDate d = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return d.getYear() == now.getYear() && d.getMonth() == now.getMonth();
    }
}
