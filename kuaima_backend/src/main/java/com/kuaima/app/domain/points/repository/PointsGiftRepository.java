package com.kuaima.app.domain.points.repository;
import java.util.Optional;import org.springframework.data.jpa.repository.JpaRepository;import com.kuaima.app.domain.points.entity.PointsGift;
public interface PointsGiftRepository extends JpaRepository<PointsGift,Long>{Optional<PointsGift> findByIdempotencyKey(String key);}
