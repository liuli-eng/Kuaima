package com.kuaima.app.domain.enterprise.service;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.entity.EnterpriseMember;
import com.kuaima.app.domain.enterprise.repository.EnterpriseMemberRepository;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.constant.EnterpriseCode;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;
import java.util.List;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** 统一解析老板端当前企业上下文和企业成员权限。 */
@Service
public class EnterpriseContextService {
    public record Context(User user, Enterprise enterprise, EnterpriseMember member) {
        public boolean ownerOrAdmin() {
            return "OWNER".equals(member.getMemberRole()) || "ADMIN".equals(member.getMemberRole());
        }
        public boolean can(String permission) {
            if (ownerOrAdmin()) return true;
            if ("VIEWER".equals(member.getMemberRole())) return permission.endsWith("_VIEW");
            if ("RECRUITER".equals(member.getMemberRole())) {
                return permission.startsWith("ORDER_") || "TALENT_VIEW".equals(permission)
                        || "ADDRESS_VIEW".equals(permission) || "ADDRESS_WRITE".equals(permission)
                        || "SETTINGS_VIEW".equals(permission) || "SETTINGS_WRITE".equals(permission);
            }
            if ("FINANCE".equals(member.getMemberRole())) {
                return permission.startsWith("SETTLEMENT_") || permission.startsWith("COUPON_")
                        || "ORDER_VIEW".equals(permission);
            }
            return false;
        }
    }

    private final UserRepository users;
    private final EnterpriseRepository enterprises;
    private final EnterpriseMemberRepository members;

    public EnterpriseContextService(UserRepository users, EnterpriseRepository enterprises,
                                    EnterpriseMemberRepository members) {
        this.users = users;
        this.enterprises = enterprises;
        this.members = members;
    }

    @Transactional
    public Context require(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser login)
                || login.id() == null || !"BOSS".equals(login.role())) {
            throw new ForbiddenBusinessException("当前登录账号没有老板端访问权限");
        }
        User user = users.findById(login.id())
                .orElseThrow(() -> new ForbiddenBusinessException("当前用户不存在"));
        EnterpriseMember member = resolveMember(login, user);
        if (member == null || !"ACTIVE".equals(member.getStatus())) {
            throw new ForbiddenBusinessException("当前账号不是有效的企业成员");
        }
        Enterprise enterprise = enterprises.findById(member.getEnterpriseId())
                .orElseThrow(() -> new ForbiddenBusinessException("企业不存在或已失效"));
        if (!"ACTIVE".equals(enterprise.getStatus())) {
            throw new ForbiddenBusinessException("企业已停用");
        }
        return new Context(user, enterprise, member);
    }

    public Context require(Authentication authentication, String permission) {
        Context context = require(authentication);
        if (!context.can(permission)) throw new ForbiddenBusinessException("无权执行当前企业操作");
        return context;
    }

    public List<EnterpriseMember> memberships(Long userId) {
        return members.findByUserIdAndStatus(userId, "ACTIVE");
    }

    /**
     * 认证审批改造前的存量账号可能只有 sys_user 企业认证字段，没有 enterprise_member
     * 关系。首次访问老板端时补齐 OWNER 关系，避免“已认证但不是有效企业成员”。
     */
    private EnterpriseMember resolveMember(LoginUser login, User user) {
        if (login.enterpriseId() != null) {
            EnterpriseMember member = members.findByEnterpriseIdAndUserId(login.enterpriseId(), login.id()).orElse(null);
            return member == null ? repairOwnerMembership(user, login.enterpriseId()) : member;
        }
        EnterpriseMember member = members.findFirstByUserIdAndStatusOrderByIdAsc(login.id(), "ACTIVE").orElse(null);
        return member == null ? repairOwnerMembership(user, null) : member;
    }

    private EnterpriseMember repairOwnerMembership(User user, Long requestedEnterpriseId) {
        if (!UserRole.hasApprovedEnterprise(user)) return null;
        if (!StringUtils.hasText(user.getCompanyCode())) {
            EnterpriseCode.ensure(user);
            users.save(user);
        }
        Enterprise enterprise = enterprises.findByCompanyCode(user.getCompanyCode()).orElseGet(() -> {
            Enterprise created = new Enterprise();
            created.setCompanyCode(user.getCompanyCode());
            created.setCompanyName(StringUtils.hasText(user.getCompanyName())
                    ? user.getCompanyName().trim() : "企业" + user.getId());
            created.setLicenseNo(user.getLicenseNo());
            created.setLegalRep(user.getLegalRep());
            created.setIndustry(user.getIndustry());
            created.setStatus("ACTIVE");
            created.setCreateBy(user.getId());
            return enterprises.save(created);
        });
        if (requestedEnterpriseId != null && !Objects.equals(requestedEnterpriseId, enterprise.getId())) return null;
        EnterpriseMember member = members.findByEnterpriseIdAndUserId(enterprise.getId(), user.getId())
                .orElseGet(EnterpriseMember::new);
        member.setEnterpriseId(enterprise.getId());
        member.setUserId(user.getId());
        member.setMemberRole("OWNER");
        member.setStatus("ACTIVE");
        member.setCreateBy(user.getId());
        return members.save(member);
    }
}
