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
import com.kuaima.app.domain.wallet.entity.Wallet;
import com.kuaima.app.domain.wallet.entity.WalletFlow;
import com.kuaima.app.domain.wallet.entity.WithDraw;
import com.kuaima.app.domain.wallet.service.WalletService;

@RestController
@RequestMapping("/wallet")
@Tag(name = "钱包", description = "钱包账户、流水、提现")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /** 我的钱包：GET /wallet/{userId}（不存在则自动创建余额 0） */
    @Operation(summary = "查询零工钱包", description = "返回该用户钱包（balance 单位分）；不存在则自动创建余额 0 的钱包")
    @GetMapping("/{userId}")
    public Result<Wallet> getWallet(@PathVariable Long userId) {
        return Result.success(walletService.getOrCreateWallet(userId));
    }

    /** 钱包流水：GET /wallet/{userId}/flows */
    @Operation(summary = "钱包流水列表", description = "返回该用户钱包流水数组（最新在前），含 direction、bizType、amount、balanceAfter、bizId")
    @GetMapping("/{userId}/flows")
    public Result<List<WalletFlow>> listFlows(@PathVariable Long userId) {
        return Result.success(walletService.listFlows(userId));
    }

    /** 我的提现记录：GET /wallet/{userId}/withdraws */
    @Operation(summary = "提现记录列表", description = "返回该用户提现单数组（最新在前）")
    @GetMapping("/{userId}/withdraws")
    public Result<List<WithDraw>> listWithdraws(@PathVariable Long userId) {
        return Result.success(walletService.listWithdraws(userId));
    }

    /**
     * 申请提现：POST /wallet/withdraw?userId=1&amount=5000&account=xxx
     * amount 单位:分；申请成功即扣减钱包余额并生成"申请中"提现单
     */
    @Operation(summary = "申请提现", description = "amount 单位分。校验金额大于 0 且不超过钱包余额；成功后立即扣减钱包余额并生成「申请中」提现单，记一笔 outcome/WITHDRAW 流水")
    @PostMapping("/withdraw")
    public Result<WithDraw> applyWithdraw(@RequestParam Long userId,
                                          @RequestParam Long amount,
                                          @RequestParam(required = false) String account,
                                          @RequestParam(required = false) String remark) {
        return Result.success(walletService.applyWithdraw(userId, amount, account, remark));
    }

    /** 模拟打款成功：POST /wallet/withdraw/{id}/payout */
    @Operation(summary = "模拟打款成功", description = "将「申请中」提现单置为「已打款」并记录打款时间。仅申请中的提现单可以打款")
    @PostMapping("/withdraw/{id}/payout")
    public Result<WithDraw> mockPayout(@PathVariable Long id) {
        return Result.success(walletService.mockPayout(id));
    }

    /** 模拟打款失败并退回余额：POST /wallet/withdraw/{id}/fail?reason=xxx */
    @Operation(summary = "模拟打款失败", description = "将「申请中」提现单置为「打款失败」，并把提现金额退回钱包，记一笔 income/WITHDRAW_REFUND 流水。仅申请中的提现单可以标记失败")
    @PostMapping("/withdraw/{id}/fail")
    public Result<WithDraw> mockPayoutFail(@PathVariable Long id,
                                           @RequestParam(required = false) String reason) {
        return Result.success(walletService.mockPayoutFail(id, reason));
    }
}
