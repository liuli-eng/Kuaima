package com.kuaima.app.controller.finance;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.wallet.entity.WalletFlow;
import com.kuaima.app.domain.wallet.repository.WalletFlowRespository;

/**
 * 费用明细与支付明细（基于钱包流水）。
 */
@RestController
public class FinanceController {

    private final WalletFlowRespository walletFlowRespository;

    public FinanceController(WalletFlowRespository walletFlowRespository) {
        this.walletFlowRespository = walletFlowRespository;
    }

    /** 费用明细（支出方向）：GET /expenses?userId=1&page=0&size=20 */
    @GetMapping("/expenses")
    public Result<List<WalletFlow>> listExpenses(@RequestParam Long userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        // 支出流水：outcome 方向
        List<WalletFlow> all = walletFlowRespository.findByUserIdAndDirectionOrderByIdDesc(userId, "outcome");
        return paginate(all, safePage, safeSize);
    }

    /** 支付明细（收入方向）：GET /payments?userId=1&page=0&size=20 */
    @GetMapping("/payments")
    public Result<List<WalletFlow>> listPayments(@RequestParam Long userId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        // 收入流水：income 方向
        List<WalletFlow> all = walletFlowRespository.findByUserIdAndDirectionOrderByIdDesc(userId, "income");
        return paginate(all, safePage, safeSize);
    }

    private Result<List<WalletFlow>> paginate(List<WalletFlow> all, int page, int size) {
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return Result.success(all.subList(from, to), page, all.size());
    }
}
