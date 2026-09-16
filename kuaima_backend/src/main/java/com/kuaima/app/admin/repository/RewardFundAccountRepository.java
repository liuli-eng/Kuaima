package com.kuaima.app.admin.repository;
import java.util.Optional;import jakarta.persistence.LockModeType;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;import com.kuaima.app.admin.entity.RewardFundAccount;
public interface RewardFundAccountRepository extends JpaRepository<RewardFundAccount,Long>{@Lock(LockModeType.PESSIMISTIC_WRITE)@Query("select a from RewardFundAccount a where a.id=:id")Optional<RewardFundAccount> findForUpdate(@Param("id")Long id);}
