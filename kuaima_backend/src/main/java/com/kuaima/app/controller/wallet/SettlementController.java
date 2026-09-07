package com.kuaima.app.controller.wallet;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.entity.Wallet;
import com.kuaima.app.domain.wallet.service.SettlementService;

/**
 * 结算（老板付钱给零工）：
 * 老板对某条"已完成"的报名记录发起结算 -> 自动算出 工资(订单日薪×工作天数)+平台服务费 -> 模拟支付，
 * 支付成功后工资进入零工钱包，零工可自行提现。所有金额单位:分。
 */
@RestController
@RequestMapping("/settle")
@Tag(name = "结算", description = "用户结算明细")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    /**
     * 发起结算：POST /settle?itemId=1&workDays=2
     * workDays 为空时按 到岗日~完成日 自动推导天数；返回"待支付"结算单（工资/服务费/实付总额）
     */
    @Operation(summary = "发起结算", description = "前置条件：报名记录须为「已完成」。系统按订单日薪(元/天) × 工作天数自动算出应付零工工资并转分；workDays 为空时按到岗日~完成日自动推导（至少 1 天）。生成「待支付」结算单，同一报名记录不允许重复发起")
    @PostMapping
    public Result<Settlement> createSettlement(@RequestParam Long itemId,
                                               @RequestParam(required = false) Integer workDays) {
        return Result.success(settlementService.createSettlement(itemId, workDays));
    }

    /** 模拟支付：POST /settle/{id}/pay，成功后工资入零工钱包 */
    @Operation(summary = "模拟支付结算单", description = "将「待支付」结算单置为「已支付」(生成模拟支付流水号、记录支付时间)，并把工资入零工钱包记一笔 income/WAGE 流水；服务费归平台。仅待支付的结算单可以支付")
    @PostMapping("/{id}/pay")
    public Result<Settlement> mockPay(@PathVariable Long id) {
        return Result.success(settlementService.mockPay(id));
    }

    /** 某订单的结算单列表：GET /settle/order/{orderId} */
    @Operation(summary = "订单结算单列表", description = "返回该订单下所有结算单数组（Settlement）")
    @GetMapping("/order/{orderId}")
    public Result<List<Settlement>> listByOrder(@PathVariable Long orderId) {
        return Result.success(settlementService.listByOrder(orderId));
    }

    /** 某零工的结算单列表：GET /settle/worker/{userId} */
    @Operation(summary = "零工结算单列表", description = "返回该零工所有结算单数组")
    @GetMapping("/worker/{userId}")
    public Result<List<Settlement>> listByWorker(@PathVariable Long userId) {
        return Result.success(settlementService.listByWorker(userId));
    }

    /** 零工钱包（方便结算后查看余额）：GET /settle/wallet/{userId} */
    @Operation(summary = "查询零工钱包", description = "等价别名 GET /wallet/{userId}，返回该用户钱包（balance 单位分）；不存在则自动创建余额 0 的钱包")
    @GetMapping("/wallet/{userId}")
    public Result<Wallet> getWorkerWallet(@PathVariable Long userId) {
        return Result.success(settlementService.getWorkerWallet(userId));
    }

    /** 结算单详情：GET /settle/{id}/detail */
    @Operation(summary = "结算单详情", description = "返回 Settlement 完整信息（含工资构成、工作天数、扣款、实发金额）")
    @GetMapping("/{id}/detail")
    public Result<Settlement> getSettlementDetail(@PathVariable Long id) {
        return Result.success(settlementService.getSettlementDetail(id));
    }
}
