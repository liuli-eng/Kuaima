package com.kuaima.app.controller.boss;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossMerchantAccount;
import com.kuaima.app.domain.boss.repository.BossMerchantAccountRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 老板端「余额查询」模块：商户号账户余额查询与充值引导。
 */
@RestController
@RequestMapping("/boss/balance")
@Tag(name = "老板-余额查询", description = "商户账户余额查询、充值引导")
@RequiredArgsConstructor
public class BossBalanceController {

    private final BossMerchantAccountRepository accountRepository;
    private final UserRepository userRepository;

    @Operation(summary = "查询商户账户余额", description = "返回默认商户账户（账户名/主体全称/商户号/余额）；未开通商户号时基于企业资料返回占位账户")
    @GetMapping
    public Result<Map<String, Object>> getBalance(Authentication authentication) {
        Long bossId = requireBossId(authentication);
        User boss = userRepository.findById(bossId).orElse(null);
        BossMerchantAccount account = accountRepository.findFirstByBossIdAndIsDefaultTrue(bossId)
                .orElseGet(() -> accountRepository.findByBossIdOrderByIdDesc(bossId).stream().findFirst()
                        .orElse(null));

        Map<String, Object> data = new LinkedHashMap<>();
        if (account != null) {
            data.put("accountName", account.getAccountName());
            data.put("subjectName", account.getSubjectName());
            data.put("merchantNo", account.getMerchantNo());
            data.put("balance", account.getBalance() == null ? 0L : account.getBalance());
            data.put("isDefault", Boolean.TRUE.equals(account.getIsDefault()));
        } else {
            // 未开通商户号：基于老板企业资料返回占位账户
            String companyName = boss != null ? boss.getCompanyName() : null;
            data.put("accountName", companyName != null ? companyName : "企业账户");
            data.put("subjectName", companyName);
            data.put("merchantNo", "");
            data.put("balance", 0L);
            data.put("isDefault", true);
        }
        return Result.success(data);
    }

    @Operation(summary = "充值引导", description = "对公转账充值需联系客服处理，返回客服提示信息（原型占位）")
    @PostMapping("/recharge")
    public Result<Map<String, Object>> recharge(Authentication authentication) {
        requireBossId(authentication);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "请对公转账后联系客服处理充值");
        data.put("serviceHotline", "400-000-0000");
        return Result.success(data);
    }

    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser
                && UserRole.BOSS.equals(loginUser.role()) && loginUser.id() != null) {
            return loginUser.id();
        }
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}
