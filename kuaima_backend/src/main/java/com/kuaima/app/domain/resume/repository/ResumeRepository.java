package com.kuaima.app.domain.resume.repository;

import com.kuaima.app.domain.resume.entity.Resume;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    @Query("""
            select r from Resume r
            where r.bossId = :bossId
              and (:tab is null or :tab = 'all'
                   or (:tab = 'fav' and r.favorite = true)
                   or (:tab = 'pending' and r.status = 'PENDING')
                   or (:tab = 'viewed' and r.status = 'VIEWED')
                   or (:tab = 'sent' and r.status = 'SENT'))
              and (:keyword is null or r.name like concat('%', :keyword, '%')
                   or r.position like concat('%', :keyword, '%')
                   or r.jobCategory like concat('%', :keyword, '%')
                   or r.experience like concat('%', :keyword, '%'))
              and (:jobCategory is null or r.jobCategory = :jobCategory)
              and (:education is null or r.education = :education)
              and (:expectedSalary is null or r.expectedSalary = :expectedSalary)
              and (:status is null or r.status = :status)
            order by r.id desc
            """)
    Page<Resume> search(@Param("bossId") Long bossId,
                        @Param("tab") String tab,
                        @Param("keyword") String keyword,
                        @Param("jobCategory") String jobCategory,
                        @Param("education") String education,
                        @Param("expectedSalary") String expectedSalary,
                        @Param("status") String status,
                        Pageable pageable);

    List<Resume> findTop5ByBossIdOrderByIdDesc(Long bossId);

    long countByBossId(Long bossId);

    long countByBossIdAndStatus(Long bossId, String status);

    long countByBossIdAndDate(Long bossId, LocalDate date);

    long countByBossIdAndFavoriteTrue(Long bossId);
}
