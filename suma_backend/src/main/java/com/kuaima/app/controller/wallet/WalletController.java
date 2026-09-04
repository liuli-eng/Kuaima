package com.kuaima.app.controller.wallet;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.wallet.entity.Wallet;
import com.kuaima.app.domain.wallet.entity.WalletFlow;
import com.kuaima.app.domain.wallet.entity.WithDraw;
import com.kuaima.app.domain.wallet.service.WalletService;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /** 我的钱包：GET /wallet/{userId}（不存在则自动创建余额 0） */
    @GetMapping("/{userId}")
    public Result<Wallet> getWallet(@PathVariable Long userId) {
        return Result.success(walletService.getOrCreateWallet(userId));
    }

    /** 钱包流水：GET /wallet/{userId}/flows */
    @GetMapping("/{userId}/flows")
    public Result<List<WalletFlow>> listFlows(@PathVariable Long userId) {
        return Result.success(walletService.listFlows(userId));
    }

    /** 我的提现记录：GET /wallet/{userId}/withdraws */
    @GetMapping("/{userId}/withdraws")
    public Result<List<WithDraw>> listWithdraws(@PathVariable Long userId) {
        return Result.success(walletService.listWithdraws(userId));
    }

    /**
     * 申请提现：POST /wallet/withdraw?userId=1&amount=5000&account=xxx
     * amount 单位:分；申请成功即扣减钱包余额并生成"申请中"提现单
     */
    @PostMapping("/withdraw")
    public Result<WithDraw> applyWithdraw(@RequestParam Long userId,
                                          @RequestParam Long amount,
                                          @RequestParam(required = false) String account,
                                          @RequestParam(required = false) String remark) {
        return Result.success(walletService.applyWithdraw(userId, amount, account, remark));
    }

    /** 模拟打款成功：POST /wallet/withdraw/{id}/payout */
    @PostMapping("/withdraw/{id}/payout")
    public Result<WithDraw> mockPayout(@PathVariable Long id) {
        return Result.success(walletService.mockPayout(id));
    }

    /** 模拟打款失败并退回余额：POST /wallet/withdraw/{id}/fail?reason=xxx */
    @PostMapping("/withdraw/{id}/fail")
    public Result<WithDraw> mockPayoutFail(@PathVariable Long id,
                                           @RequestParam(required = false) String reason) {
        return Result.success(walletService.mockPayoutFail(id, reason));
    }
}
