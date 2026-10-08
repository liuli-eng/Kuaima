package com.kuaima.app.domain.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.payroll.constant.PayrollConstants;
import com.kuaima.app.domain.payroll.entity.PayrollDetail;
import com.kuaima.app.domain.payroll.entity.PayrollOrder;
import com.kuaima.app.domain.project.entity.ProjectMember;
import com.kuaima.app.domain.project.repository.ProjectMemberRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.service.WalletService;

/**
 * 发薪付款支撑服务：收款人解析 + 分→元入账。
 *
 * <p>注意：本类不开启事务，所有方法都必须在调用方（{@code PayrollService.approve}）的事务内执行，
 * 以保证「扣老板账户 + 逐人入钱包 + 回填明细」的原子性。
 */
@Service
public class PayrollPayService {

    /** 分 → 元 的换算基准。老板余额以「分」存储，零工钱包以「元」存储。 */
    private static final BigDecimal FEN_PER_YUAN = new BigDecimal("100");

    /** 无收款账号时的失败原因文案。 */
    public static final String REMARK_NO_PAYEE = "无收款账号：项目成员未绑定 userId 且手机号无唯一用户匹配";

    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    public PayrollPayService(ProjectMemberRepository memberRepository,
                             UserRepository userRepository,
                             WalletService walletService) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    /** 收款人解析结果：payable 可入账，unpayable 无收款账号（降级为 failed）。 */
    public record PayeeResolution(List<PayrollDetail> payable, List<PayrollDetail> unpayable) {
    }

    /**
     * 解析整单收款人。
     *
     * <p>白名单校验：每笔明细必须能在该项目的 {@code project_member} 中定位，否则抛异常整单拒付
     * （与「余额不足整单拒付」一致，绝不做部分付款）。收款人一律取自 {@code project_member}，
     * 不信任明细里传入的 userId，避免串号。
     *
     * @param projectId 发薪单关联的项目 id
     * @param details   发薪明细
     */
    public PayeeResolution resolvePayees(Long projectId, List<PayrollDetail> details) {
        if (projectId == null) {
            throw new IllegalArgumentException("发薪单未关联项目，无法校验收款人，请先补充关联项目");
        }
        List<ProjectMember> members = memberRepository.findByProjectId(projectId);
        Set<Long> memberUserIds = new HashSet<>();
        Map<String, ProjectMember> memberByPhone = new HashMap<>();
        for (ProjectMember member : members) {
            if (member.getUserId() != null) {
                memberUserIds.add(member.getUserId());
            }
            if (StringUtils.hasText(member.getPhone())) {
                memberByPhone.putIfAbsent(member.getPhone().trim(), member);
            }
        }

        List<PayrollDetail> payable = new ArrayList<>();
        List<PayrollDetail> unpayable = new ArrayList<>();
        for (PayrollDetail detail : details) {
            ProjectMember member = locateMember(detail, members, memberUserIds, memberByPhone);
            if (member == null) {
                throw new IllegalArgumentException("发薪明细「" + safeName(detail)
                        + "」不属于该项目的成员，整单拒绝审批");
            }
            Long payeeId = resolvePayeeUserId(member);
            if (payeeId == null) {
                detail.setRemark(REMARK_NO_PAYEE);
                unpayable.add(detail);
            } else {
                detail.setUserId(payeeId);
                payable.add(detail);
            }
        }
        return new PayeeResolution(payable, unpayable);
    }

    /**
     * 单笔入账：分→元写入零工钱包，并回填明细的转账记录字段。
     * 必须在调用方事务内执行。
     */
    public void pay(PayrollDetail detail, PayrollOrder order) {
        walletService.credit(detail.getUserId(), fenToYuan(detail.getAmount(), detail.getName()),
                WalletService.BIZ_WAGE, order.getId(),
                "发薪单 " + order.getOrderNo() + " 工资");
        detail.setOrderNo(order.getOrderNo());
        detail.setAccount("零工钱包 · " + detail.getUserId());
        detail.setPayTime(new Date());
        detail.setStatus(PayrollConstants.DETAIL_SUCCESS);
    }

    /** 分 → 元，保留两位小数。这是本项目唯一允许出现金额单位换算的地方。 */
    public static BigDecimal fenToYuan(Long fen, String who) {
        if (fen == null || fen <= 0) {
            throw new IllegalArgumentException(safeName(who) + " 的应发金额非法（分）：" + fen);
        }
        return BigDecimal.valueOf(fen).divide(FEN_PER_YUAN, 2, RoundingMode.HALF_UP);
    }

    /** 在项目成员中定位明细对应的人：先用 userId，再用手机号。 */
    private ProjectMember locateMember(PayrollDetail detail, List<ProjectMember> members,
                                       Set<Long> memberUserIds, Map<String, ProjectMember> memberByPhone) {
        if (detail.getUserId() != null && memberUserIds.contains(detail.getUserId())) {
            for (ProjectMember member : members) {
                if (Objects.equals(member.getUserId(), detail.getUserId())) {
                    return member;
                }
            }
        }
        if (StringUtils.hasText(detail.getPhone())) {
            return memberByPhone.get(detail.getPhone().trim());
        }
        return null;
    }

    /** 收款账号：project_member.userId 优先；为空则回退手机号匹配 sys_user（必须唯一命中）。 */
    private Long resolvePayeeUserId(ProjectMember member) {
        if (member.getUserId() != null) {
            return member.getUserId();
        }
        if (!StringUtils.hasText(member.getPhone())) {
            return null;
        }
        List<User> users = userRepository.findByPhone(member.getPhone().trim());
        return users != null && users.size() == 1 ? users.get(0).getId() : null;
    }

    private static String safeName(PayrollDetail detail) {
        return detail == null || !StringUtils.hasText(detail.getName()) ? "未填写姓名" : detail.getName();
    }

    private static String safeName(String name) {
        return StringUtils.hasText(name) ? name : "该成员";
    }
}