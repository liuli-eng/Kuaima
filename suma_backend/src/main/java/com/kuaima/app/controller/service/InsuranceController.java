package com.kuaima.app.controller.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.insurance.entity.Insurance;
import com.kuaima.app.domain.insurance.repository.InsuranceRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 保险服务。
 */
@RestController
@RequestMapping("/insurance")
public class InsuranceController {

    private final InsuranceRepository insuranceRepository;

    public InsuranceController(InsuranceRepository insuranceRepository) {
        this.insuranceRepository = insuranceRepository;
    }

    /** 保险记录列表：GET /insurance?userId=1 */
    @GetMapping
    public Result<List<Insurance>> listInsurance(@RequestParam Long userId) {
        return Result.success(insuranceRepository.findByUserIdOrderByIdDesc(userId));
    }

    /** 购买保险：POST /insurance */
    @PostMapping
    @Transactional
    public Result<Insurance> buyInsurance(@RequestBody Map<String, Object> body) {
        Insurance insurance = new Insurance();
        insurance.setUserId(body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null);
        insurance.setUserType(body.get("userType") != null ? body.get("userType").toString() : "WORKER");
        insurance.setOrderId(body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null);
        insurance.setType(body.get("type") != null ? body.get("type").toString() : "意外险");
        insurance.setAmount(body.get("amount") != null ? Double.valueOf(body.get("amount").toString()) : 0.0);
        insurance.setPremium(body.get("premium") != null ? Double.valueOf(body.get("premium").toString()) : 0.0);
        insurance.setStatus("ACTIVE");
        insurance.setStartTime(new Timestamp(System.currentTimeMillis()));
        return Result.success(insuranceRepository.save(insurance));
    }

    /** 保险详情：GET /insurance/{id} */
    @GetMapping("/{id}")
    public Result<Insurance> getInsurance(@PathVariable Long id) {
        return Result.success(insuranceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("保险记录不存在: " + id)));
    }
}
