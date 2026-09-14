package com.kuaima.app.domain.project.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.project.constant.ProjectConstants;
import com.kuaima.app.domain.project.entity.OnboardApply;
import com.kuaima.app.domain.project.repository.OnboardRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OnboardService {

    private final OnboardRepository onboardRepository;

    public OnboardService(OnboardRepository onboardRepository) {
        this.onboardRepository = onboardRepository;
    }

    public List<OnboardApply> list(Long projectId, String status) {
        if (ProjectConstants.ONBOARD_PENDING.equals(status)
                || ProjectConstants.ONBOARD_PASSED.equals(status)
                || ProjectConstants.ONBOARD_REJECTED.equals(status)) {
            return onboardRepository.findByProjectIdAndStatus(projectId, status);
        }
        return onboardRepository.findByProjectId(projectId);
    }

    public OnboardApply getOrThrow(Long id) {
        return onboardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("入职申请不存在: " + id));
    }

    @Transactional
    public OnboardApply pass(Long id, String reviewer) {
        OnboardApply apply = getOrThrow(id);
        apply.setStatus(ProjectConstants.ONBOARD_PASSED);
        apply.setReviewer(reviewer);
        apply.setReviewTime(new java.util.Date());
        return onboardRepository.save(apply);
    }

    @Transactional
    public OnboardApply reject(Long id, String reviewer) {
        OnboardApply apply = getOrThrow(id);
        apply.setStatus(ProjectConstants.ONBOARD_REJECTED);
        apply.setReviewer(reviewer);
        apply.setReviewTime(new java.util.Date());
        return onboardRepository.save(apply);
    }

    /** 入职申请统计：全部 / 审核中 / 已通过 / 已拒绝。 */
    public Map<String, Long> stats(Long projectId) {
        Map<String, Long> result = new LinkedHashMap<>();
        List<OnboardApply> all = onboardRepository.findByProjectId(projectId);
        long pending = all.stream().filter(a -> ProjectConstants.ONBOARD_PENDING.equals(a.getStatus())).count();
        long passed = all.stream().filter(a -> ProjectConstants.ONBOARD_PASSED.equals(a.getStatus())).count();
        long rejected = all.stream().filter(a -> ProjectConstants.ONBOARD_REJECTED.equals(a.getStatus())).count();
        result.put("all", (long) all.size());
        result.put("pending", pending);
        result.put("passed", passed);
        result.put("rejected", rejected);
        return result;
    }
}
