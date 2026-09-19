package com.kuaima.app.domain.resume.service;

import com.kuaima.app.common.BusinessHttpException;
import com.kuaima.app.domain.resume.entity.Resume;
import com.kuaima.app.domain.resume.entity.ResumeExperience;
import com.kuaima.app.domain.resume.entity.ResumeImportRecord;
import com.kuaima.app.domain.resume.repository.ResumeExperienceRepository;
import com.kuaima.app.domain.resume.repository.ResumeImportRecordRepository;
import com.kuaima.app.domain.resume.repository.ResumeRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepo;
    private final ResumeExperienceRepository expRepo;
    private final ResumeImportRecordRepository importRepo;

    public ResumeService(ResumeRepository resumeRepo,
                         ResumeExperienceRepository expRepo,
                         ResumeImportRecordRepository importRepo) {
        this.resumeRepo = resumeRepo;
        this.expRepo = expRepo;
        this.importRepo = importRepo;
    }

    public Map<String, Object> stats(Long bossId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", resumeRepo.countByBossId(bossId));
        stats.put("todayNew", resumeRepo.countByBossIdAndDate(bossId, LocalDate.now()));
        stats.put("pending", resumeRepo.countByBossIdAndStatus(bossId, "PENDING"));
        stats.put("sent", resumeRepo.countByBossIdAndStatus(bossId, "SENT"));
        stats.put("favorite", resumeRepo.countByBossIdAndFavoriteTrue(bossId));
        return stats;
    }

    public List<Resume> latest(Long bossId) {
        return resumeRepo.findTop5ByBossIdOrderByIdDesc(bossId);
    }

    public Page<Resume> list(Long bossId, String tab, String keyword, String jobCategory,
                             String education, String expectedSalary, String status,
                             int page, int size) {
        String normalizedTab = tab == null || tab.isBlank() ? "all" : tab.toLowerCase();
        return resumeRepo.search(bossId, normalizedTab,
                blankToNull(keyword), blankToNull(jobCategory), blankToNull(education),
                blankToNull(expectedSalary), blankToNull(status),
                PageRequest.of(page, size));
    }

    public Map<String, Object> detail(Long bossId, Long resumeId) {
        Resume resume = requireOwned(bossId, resumeId);
        List<ResumeExperience> experiences = expRepo.findByResumeIdOrderByIdAsc(resumeId);
        Map<String, Object> detail = new HashMap<>();
        detail.put("resume", resume);
        detail.put("edu", experiences.stream().filter(e -> "EDU".equals(e.getType())).toList());
        detail.put("work", experiences.stream().filter(e -> "WORK".equals(e.getType())).toList());
        detail.put("project", experiences.stream().filter(e -> "PROJECT".equals(e.getType())).toList());
        return detail;
    }

    @Transactional
    public Resume toggleFavorite(Long bossId, Long resumeId) {
        Resume resume = requireOwned(bossId, resumeId);
        resume.setFavorite(!Boolean.TRUE.equals(resume.getFavorite()));
        return resumeRepo.save(resume);
    }

    @Transactional
    public Resume updateStatus(Long bossId, Long resumeId, String status) {
        Resume resume = requireOwned(bossId, resumeId);
        resume.setStatus(status);
        return resumeRepo.save(resume);
    }

    @Transactional
    public void delete(Long bossId, Long resumeId) {
        Resume resume = requireOwned(bossId, resumeId);
        resumeRepo.delete(resume);
        expRepo.deleteByResumeId(resumeId);
    }

    @Transactional
    public Map<String, Object> batch(Long bossId, List<Long> ids, String action) {
        List<Resume> resumes = resumeRepo.findAllById(ids).stream()
                .filter(r -> r.getBossId().equals(bossId)).toList();
        switch (action) {
            case "read" -> resumes.forEach(r -> {
                r.setStatus("VIEWED");
                resumeRepo.save(r);
            });
            case "fav" -> resumes.forEach(r -> {
                r.setFavorite(true);
                resumeRepo.save(r);
            });
            case "del" -> resumes.forEach(r -> delete(bossId, r.getId()));
            case "process" -> resumes.forEach(r -> {
                r.setStatus("SENT");
                resumeRepo.save(r);
            });
            default -> throw new BusinessHttpException(HttpStatus.BAD_REQUEST, "不支持的操作类型");
        }
        return Map.of("count", resumes.size(), "action", action);
    }

    @Transactional
    public ResumeImportRecord createImport(Long bossId, Long enterpriseId, String fileName,
                                           String fileUrl, boolean success, String failReason) {
        ResumeImportRecord record = new ResumeImportRecord();
        record.setBossId(bossId);
        record.setEnterpriseId(enterpriseId);
        record.setFileName(fileName);
        record.setFileUrl(fileUrl);
        record.setStatus(success ? "SUCCESS" : "FAILED");
        record.setFailReason(failReason);
        return importRepo.save(record);
    }

    public List<ResumeImportRecord> listImports(Long bossId) {
        return importRepo.findTop20ByBossIdOrderByIdDesc(bossId);
    }

    private Resume requireOwned(Long bossId, Long resumeId) {
        Resume resume = resumeRepo.findById(resumeId)
                .orElseThrow(() -> new BusinessHttpException(HttpStatus.BAD_REQUEST, "简历不存在"));
        if (!resume.getBossId().equals(bossId)) {
            throw new BusinessHttpException(HttpStatus.FORBIDDEN, "无权操作该简历");
        }
        return resume;
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
