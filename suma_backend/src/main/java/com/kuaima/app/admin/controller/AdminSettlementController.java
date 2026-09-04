package com.kuaima.app.admin.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.domain.wallet.service.SettlementService;

/**
 * 后台结算管理（列出全部结算单、手动触发结算、批量结算）
 */
@RestController
@RequestMapping("/admin/settlements")
public class AdminSettlementController {

    private final SettlementRespository settlementRepository;
    private final SettlementService settlementService;

    public AdminSettlementController(SettlementRespository settlementRepository,
                                      SettlementService settlementService) {
        this.settlementRepository = settlementRepository;
        this.settlementService = settlementService;
    }

    /** 结算单列表 */
    @GetMapping
    public Result<Page<Settlement>> list(@RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Settlement> result = settlementRepository.findAll(pageable);
        return Result.success(result, page, result.getTotalElements());
    }

    /** 结算单详情 */
    @GetMapping("/{id}")
    public Result<Settlement> get(@PathVariable Long id) {
        return Result.success(settlementRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("结算单不存在: " + id)));
    }

    /** admin 手动结算（模拟支付） */
    @PostMapping("/{id}/pay")
    public Result<Settlement> pay(@PathVariable Long id) {
        return Result.success(settlementService.mockPay(id));
    }
}
