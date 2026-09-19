package com.kuaima.app.domain.position.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.position.entity.BossPosition;
import com.kuaima.app.domain.position.repository.BossPositionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BossPositionService {

    private final BossPositionRepository positionRepository;

    public Page<BossPosition> listPositions(String keyword, String status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        return positionRepository.searchByKeywordAndStatus(searchKeyword, status, pageable);
    }

    public BossPosition getPosition(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("岗位不存在: " + id));
    }

    public BossPosition getPositionByCode(String code) {
        return positionRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("岗位不存在: " + code));
    }

    @Transactional
    public BossPosition createPosition(BossPosition position) {
        if (positionRepository.existsByCode(position.getCode())) {
            throw new IllegalArgumentException("岗位编码已存在: " + position.getCode());
        }
        if (position.getStatus() == null) {
            position.setStatus("on");
        }
        if (position.getApplyCount() == null) {
            position.setApplyCount(0);
        }
        if (position.getInterviewCount() == null) {
            position.setInterviewCount(0);
        }
        if (position.getHiredCount() == null) {
            position.setHiredCount(0);
        }
        if (position.getHotCount() == null) {
            position.setHotCount(0);
        }
        if (position.getHireCount() == null) {
            position.setHireCount(0);
        }
        if (position.getPublishTime() == null) {
            position.setPublishTime(LocalDateTime.now());
        }
        return positionRepository.save(position);
    }

    @Transactional
    public BossPosition updatePosition(Long id, BossPosition updates) {
        BossPosition existing = getPosition(id);
        existing.setName(updates.getName());
        existing.setCode(updates.getCode());
        existing.setCategory(updates.getCategory());
        existing.setDepartment(updates.getDepartment());
        existing.setLocation(updates.getLocation());
        existing.setHireCount(updates.getHireCount());
        existing.setSalaryRange(updates.getSalaryRange());
        existing.setExperience(updates.getExperience());
        existing.setEducation(updates.getEducation());
        existing.setDescription(updates.getDescription());
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }
        return positionRepository.save(existing);
    }

    @Transactional
    public void deletePosition(Long id) {
        BossPosition position = getPosition(id);
        positionRepository.delete(position);
    }

    @Transactional
    public BossPosition toggleStatus(Long id) {
        BossPosition position = getPosition(id);
        if ("on".equals(position.getStatus())) {
            position.setStatus("off");
        } else {
            position.setStatus("on");
        }
        return positionRepository.save(position);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", positionRepository.count());
        stats.put("activeCount", positionRepository.countByStatus("on"));
        stats.put("inactiveCount", positionRepository.countByStatus("off"));
        stats.put("hireCount", positionRepository.sumHireCount());
        stats.put("applyCount", positionRepository.sumApplyCount());
        stats.put("interviewCount", positionRepository.sumInterviewCount());
        stats.put("hiredCount", positionRepository.sumHiredCount());
        return stats;
    }

    public List<BossPosition> getHotRankings(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "hotCount"));
        return positionRepository.findTopByHotCount(pageable);
    }
}
