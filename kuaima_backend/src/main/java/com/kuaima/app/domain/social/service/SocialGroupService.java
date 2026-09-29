package com.kuaima.app.domain.social.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.social.dto.SocialGroupJoinRequest;
import com.kuaima.app.domain.social.dto.SocialGroupView;
import com.kuaima.app.domain.social.entity.SocialGroup;
import com.kuaima.app.domain.social.entity.SocialGroupMember;
import com.kuaima.app.domain.social.repository.SocialGroupMemberRepository;
import com.kuaima.app.domain.social.repository.SocialGroupRepository;
import com.kuaima.app.domain.user.constant.CertificationStatus;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SocialGroupService {

    private static final String WORKER = "WORKER";
    private static final String ACTIVE = "ACTIVE";
    private static final int DEFAULT_MEMBER_LIMIT = 200;

    private final SocialGroupRepository groups;
    private final SocialGroupMemberRepository members;
    private final UserRepository users;

    public SocialGroupService(SocialGroupRepository groups,
                              SocialGroupMemberRepository members,
                              UserRepository users) {
        this.groups = groups;
        this.members = members;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Page<SocialGroupView> list(LoginUser login, String role, String status, int page, int size) {
        User current = currentWorker(login);
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 或 size 参数无效");
        }
        String requestedRole = normalize(role, WORKER);
        String requestedStatus = normalize(status, ACTIVE);
        if (!WORKER.equals(requestedRole)) {
            throw new IllegalArgumentException("零工端 role 必须为 WORKER");
        }
        if (!ACTIVE.equals(requestedStatus)) {
            throw new IllegalArgumentException("零工端 status 必须为 ACTIVE");
        }
        return groups.findVisibleGroups(WORKER, ACTIVE, PageRequest.of(page, size))
                .map(group -> view(group, current.getId()));
    }

    @Transactional
    public SocialGroupView join(Long groupId, SocialGroupJoinRequest request, LoginUser login) {
        User current = currentWorker(login);
        if (!isRealnameApproved(current)) {
            throw new ForbiddenBusinessException("请先完成实名认证");
        }
        if (request == null || !Boolean.TRUE.equals(request.confirmed())) {
            throw new IllegalArgumentException("请确认已扫码加入群聊");
        }
        String source = StringUtils.hasText(request.source()) ? request.source().trim() : "WORKER_GROUP_PAGE";
        if (source.length() > 100) {
            throw new IllegalArgumentException("source 长度不能超过100");
        }

        SocialGroup group = groups.findByIdForUpdate(groupId)
                .orElseThrow(() -> new EntityNotFoundException("社群不存在: " + groupId));
        requireJoinable(group);

        SocialGroupMember existing = members.findByGroupIdAndUserId(groupId, current.getId()).orElse(null);
        if (existing == null) {
            int count = count(group);
            int limit = limit(group);
            if (count >= limit) {
                throw new IllegalStateException("该群已满员");
            }
            SocialGroupMember member = new SocialGroupMember();
            member.setGroupId(groupId);
            member.setUserId(current.getId());
            member.setSource(source);
            member.setConfirmed(true);
            member.setJoinedAt(LocalDateTime.now());
            members.save(member);
            group.setMemberCount(count + 1);
            groups.save(group);
        } else if (!Boolean.TRUE.equals(existing.getConfirmed())) {
            existing.setConfirmed(true);
            existing.setSource(source);
            existing.setJoinedAt(LocalDateTime.now());
            members.save(existing);
        }
        return view(group, current.getId());
    }

    private void requireJoinable(SocialGroup group) {
        if (Boolean.TRUE.equals(group.getDeleted())
                || !WORKER.equals(normalize(group.getRole(), WORKER))
                || !ACTIVE.equals(normalize(group.getStatus(), ACTIVE))) {
            throw new IllegalStateException("该群当前不可加入");
        }
    }

    private SocialGroupView view(SocialGroup group, Long userId) {
        int count = count(group);
        int limit = limit(group);
        boolean joined = members.findByGroupIdAndUserId(group.getId(), userId)
                .map(SocialGroupMember::getConfirmed).orElse(false);
        List<Long> memberUserIds = members.findUserIdsByGroupId(group.getId(), PageRequest.of(0, 5));
        List<String> avatars = users.findAllById(memberUserIds).stream()
                .map(User::getAvatar)
                .filter(StringUtils::hasText)
                .limit(5)
                .toList();
        LocalDateTime createdAt = group.getCreatedAt();
        if (createdAt == null && group.getDate() != null) {
            createdAt = group.getDate().atStartOfDay();
        }
        return new SocialGroupView(group.getId(), group.getName(), group.getCategory(), group.getDescription(),
                group.getQrcodeUrl(), count, limit, createdAt, normalize(group.getStatus(), ACTIVE), joined,
                count >= limit, avatars, Objects.requireNonNullElse(group.getSort(), 0));
    }

    private User currentWorker(LoginUser login) {
        if (login == null || login.id() == null) {
            throw new ForbiddenBusinessException("请先登录");
        }
        User user = users.findById(login.id())
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + login.id()));
        if (!UserRole.isWorkerIdentity(user)) {
            throw new ForbiddenBusinessException("当前用户不是零工身份");
        }
        return user;
    }

    private boolean isRealnameApproved(User user) {
        return CertificationStatus.APPROVED.equals(user.getRealnameStatus())
                || ("REALNAME".equalsIgnoreCase(user.getCertType()) && "已通过".equals(user.getCertStatus()));
    }

    private int count(SocialGroup group) {
        return Math.max(Objects.requireNonNullElse(group.getMemberCount(), 0), 0);
    }

    private int limit(SocialGroup group) {
        int value = Objects.requireNonNullElse(group.getMemberLimit(), DEFAULT_MEMBER_LIMIT);
        return value > 0 ? value : DEFAULT_MEMBER_LIMIT;
    }

    private String normalize(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : fallback;
    }
}
