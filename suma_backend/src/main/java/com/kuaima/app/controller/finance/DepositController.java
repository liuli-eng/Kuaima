package com.kuaima.app.controller.finance;

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
import com.kuaima.app.domain.deposit.entity.Deposit;
import com.kuaima.app.domain.deposit.repository.DepositRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 押金管理。
 */
@RestController
@RequestMapping("/deposits")
public class DepositController {

    private final DepositRepository depositRepository;

    public DepositController(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    /** 押金记录：GET /deposits/{userId} */
    @GetMapping("/{userId}")
    public Result<List<Deposit>> listDeposits(@PathVariable Long userId) {
        return Result.success(depositRepository.findByUserId(userId));
    }

    /** 缴纳押金：POST /deposits  body: { "userId": 1, "amount": 100.0 } */
    @PostMapping
    @Transactional
    public Result<Deposit> payDeposit(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
        Double amount = body.get("amount") != null ? Double.valueOf(body.get("amount").toString()) : null;
        if (userId == null || amount == null || amount <= 0) {
            throw new IllegalArgumentException("userId 和 amount(>0) 不能为空");
        }
        Deposit deposit = new Deposit();
        deposit.setUserId(userId);
        deposit.setAmount(amount);
        deposit.setStatus("PAID");
        deposit.setPayTime(new Timestamp(System.currentTimeMillis()));
        return Result.success(depositRepository.save(deposit));
    }

    /** 申请退还：POST /deposits/{id}/refund */
    @PostMapping("/{id}/refund")
    @Transactional
    public Result<Deposit> refundDeposit(@PathVariable Long id) {
        Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("押金单不存在: " + id));
        if (!"PAID".equals(deposit.getStatus())) {
            throw new IllegalStateException("仅已支付的押金可以退还");
        }
        deposit.setStatus("REFUNDED");
        deposit.setRefundTime(new Timestamp(System.currentTimeMillis()));
        return Result.success(depositRepository.save(deposit));
    }
}
