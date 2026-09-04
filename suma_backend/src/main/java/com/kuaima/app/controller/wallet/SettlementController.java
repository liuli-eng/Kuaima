package com.kuaima.app.controller.wallet;

import java.util.List;

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
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    /**
     * 发起结算：POST /settle?itemId=1&workDays=2
     * workDays 为空时按 到岗日~完成日 自动推导天数；返回"待支付"结算单（工资/服务费/实付总额）
     */
    @PostMapping
    public Result<Settlement> createSettlement(@RequestParam Long itemId,
                                               @RequestParam(required = false) Integer workDays) {
        return Result.success(settlementService.createSettlement(itemId, workDays));
    }

    /** 模拟支付：POST /settle/{id}/pay，成功后工资入零工钱包 */
    @PostMapping("/{id}/pay")
    public Result<Settlement> mockPay(@PathVariable Long id) {
        return Result.success(settlementService.mockPay(id));
    }

    /** 某订单的结算单列表：GET /settle/order/{orderId} */
    @GetMapping("/order/{orderId}")
    public Result<List<Settlement>> listByOrder(@PathVariable Long orderId) {
        return Result.success(settlementService.listByOrder(orderId));
    }

    /** 某零工的结算单列表：GET /settle/worker/{userId} */
    @GetMapping("/worker/{userId}")
    public Result<List<Settlement>> listByWorker(@PathVariable Long userId) {
        return Result.success(settlementService.listByWorker(userId));
    }

    /** 零工钱包（方便结算后查看余额）：GET /settle/wallet/{userId} */
    @GetMapping("/wallet/{userId}")
    public Result<Wallet> getWorkerWallet(@PathVariable Long userId) {
        return Result.success(settlementService.getWorkerWallet(userId));
    }

    /** 结算单详情：GET /settle/{id}/detail */
    @GetMapping("/{id}/detail")
    public Result<Settlement> getSettlementDetail(@PathVariable Long id) {
        return Result.success(settlementService.getSettlementDetail(id));
    }
}
