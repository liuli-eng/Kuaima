package com.kuaima.app.controller.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "电子合同", description = "用户电子合同查询与签署")
public class ContractController {

    private final ContractRepository contractRepository;

    public ContractController(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    /** 合同列表：GET /boss/contracts?bossId=1 */
    @Operation(summary = "合同列表", description = "按老板 id 查询合同，按 id 倒序；bossId 必填")
    @GetMapping
    public Result<List<Contract>> listContracts(@RequestParam Long bossId) {
        return Result.success(contractRepository.findByBossIdOrderByIdDesc(bossId));
    }

    /** 创建合同：POST /boss/contracts */
    @Operation(summary = "创建合同", description = "body：{\"bossId\":1,\"workerId\":2,\"orderId\":3,\"content\":\"合同内容\"}；字段均可选，创建即置 status=PENDING 待签署")
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
    @Operation(summary = "合同详情", description = "按 id 查询合同；不存在抛出 EntityNotFoundException")
    @GetMapping("/{id}")
    public Result<Contract> getContract(@PathVariable Long id) {
        return Result.success(contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("合同不存在: " + id)));
    }
}
