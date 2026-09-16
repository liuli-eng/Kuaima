package com.kuaima.app.domain.points.repository;
import java.util.List;import java.util.Optional;import org.springframework.data.jpa.repository.JpaRepository;import com.kuaima.app.domain.points.entity.PointsPackage;
public interface PointsPackageRepository extends JpaRepository<PointsPackage,Long>{List<PointsPackage> findByEnabledTrueOrderByPointsAsc();Optional<PointsPackage> findByIdAndEnabledTrue(Long id);}
