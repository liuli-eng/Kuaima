package com.kuaima.app.domain.talentpool.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.talentpool.entity.TalentHire;
import com.kuaima.app.domain.talentpool.entity.TalentPool;
import com.kuaima.app.domain.talentpool.entity.TalentReview;
import com.kuaima.app.domain.talentpool.repository.TalentHireRepository;
import com.kuaima.app.domain.talentpool.repository.TalentPoolRepository;
import com.kuaima.app.domain.talentpool.repository.TalentReviewRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TalentPoolService {

    private final TalentPoolRepository poolRepository;
    private final TalentHireRepository hireRepository;
    private final TalentReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public Page<TalentPool> listTalents(Long bossId, String keyword, String type, Boolean favoriteOnly,
                                        int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String safeType = StringUtils.hasText(type) ? type.trim() : null;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return poolRepository.searchByBoss(bossId, searchKeyword, safeType, favoriteOnly, pageable);
    }

    public Map<String, Object> getTalentDetail(Long bossId, Long id) {
        TalentPool talent = poolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("人才不存在: " + id));
        if (!talent.getBossId().equals(bossId)) {
            throw new com.kuaima.app.common.ForbiddenBusinessException("无权查看其他老板的人才库");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("talent", talent);
        detail.put("reviews", reviewRepository.findByTalentIdOrderByIdDesc(id));
        detail.put("hireRecords", hireRepository.findByTalentIdOrderByIdDesc(id));
        return detail;
    }

    @Transactional
    public TalentPool addToPool(Long bossId, TalentPool talent) {
        talent.setBossId(bossId);
        if (talent.getType() == null) {
            talent.setType("new");
        }
        if (talent.getFavorite() == null) {
            talent.setFavorite(false);
        }
        if (talent.getCompletedOrders() == null) {
            talent.setCompletedOrders(0);
        }
        if (talent.getSource() == null) {
            talent.setSource("manual");
        }
        if (talent.getAvatarColor() == null) {
            talent.setAvatarColor("#FF6B35,#FF8C5A");
        }
        return poolRepository.save(talent);
    }

    @Transactional
    public Map<String, Object> toggleFavorite(Long bossId, Long id, Boolean favorite) {
        TalentPool talent = requireOwnedTalent(bossId, id);
        boolean target = favorite == null ? !Boolean.TRUE.equals(talent.getFavorite()) : favorite;
        talent.setFavorite(target);
        poolRepository.save(talent);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("favorite", target);
        return result;
    }

    @Transactional
    public TalentHire hireTalent(Long bossId, Long id, Long jobId, String jobName,
                                  LocalDate workDate, String note) {
        TalentPool talent = requireOwnedTalent(bossId, id);
        TalentHire hire = new TalentHire();
        hire.setBossId(bossId);
        hire.setTalentId(id);
        hire.setWorkerId(talent.getWorkerId());
        hire.setJobId(jobId);
        hire.setJobName(jobName);
        hire.setWorkDate(workDate);
        hire.setNote(note);
        hire.setStatus("pending");
        TalentHire saved = hireRepository.save(hire);

        if (talent.getWorkerId() != null) {
            User boss = userRepository.findById(bossId).orElse(null);
            String bossName = boss != null && StringUtils.hasText(boss.getCompanyName())
                    ? boss.getCompanyName()
                    : (boss != null && StringUtils.hasText(boss.getNickname()) ? boss.getNickname() : "老板");
            messageService.sendToUser(talent.getWorkerId(), UserRole.USER, "BOSS_INVITE", "雇佣通知",
                    bossName + " 向您发起了雇佣邀请（" + (jobName == null ? "未指定岗位" : jobName) + "），快去看看吧！",
                    BizType.ORDER, jobId, Map.of("bossName", bossName,
                            "talentId", id, "jobId", jobId == null ? "" : jobId));
        }
        return saved;
    }

    @Transactional
    public void removeTalent(Long bossId, Long id) {
        TalentPool talent = requireOwnedTalent(bossId, id);
        reviewRepository.findByTalentIdOrderByIdDesc(id)
                .forEach(reviewRepository::delete);
        hireRepository.findByTalentIdOrderByIdDesc(id)
                .forEach(hireRepository::delete);
        poolRepository.delete(talent);
    }

    public List<TalentReview> listReviews(Long talentId) {
        return reviewRepository.findByTalentIdOrderByIdDesc(talentId);
    }

    private TalentPool requireOwnedTalent(Long bossId, Long id) {
        TalentPool talent = poolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("人才不存在: " + id));
        if (!talent.getBossId().equals(bossId)) {
            throw new com.kuaima.app.common.ForbiddenBusinessException("无权操作其他老板的人才库");
        }
        return talent;
    }
}
