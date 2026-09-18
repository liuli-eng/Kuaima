package com.kuaima.app.domain.academy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.academy.entity.AcademySimulateVideo;

public interface AcademySimulateVideoRepository extends JpaRepository<AcademySimulateVideo, Long> {
    List<AcademySimulateVideo> findAllByOrderBySortAscIdAsc();

    Optional<AcademySimulateVideo> findByUrl(String url);
}
