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
import com.kuaima.app.domain.contract.entity.Contract;
import com.kuaima.app.domain.contract.repository.ContractRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 电子合同。
 */
@RestController
@RequestMapping("/boss/contracts")
public class ContractController {

    private final ContractRepository contractRepository;

    public ContractController(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    /** 合同列表：GET /boss/contracts?bossId=1 */
    @GetMapping
    public Result<List<Contract>> listContracts(@RequestParam Long bossId) {
        return Result.success(contractRepository.findByBossIdOrderByIdDesc(bossId));
    }

    /** 创建合同：POST /boss/contracts */
    @PostMapping
    @Transactional
    public Result<Contract> createContract(@RequestBody Map<String, Object> body) {
        Contract contract = new Contract();
        contract.setBossId(body.get("bossId") != null ? Long.valueOf(body.get("bossId").toString()) : null);
        contract.setWorkerId(body.get("workerId") != null ? Long.valueOf(body.get("workerId").toString()) : null);
        contract.setOrderId(body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null);
        contract.setContent(body.get("content") != null ? body.get("content").toString() : null);
        contract.setStatus("PENDING");
        return Result.success(contractRepository.save(contract));
    }

    /** 合同详情：GET /boss/contracts/{id} */
    @GetMapping("/{id}")
    public Result<Contract> getContract(@PathVariable Long id) {
        return Result.success(contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("合同不存在: " + id)));
    }
}
