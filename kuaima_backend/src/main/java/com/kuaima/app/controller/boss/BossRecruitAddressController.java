package com.kuaima.app.controller.boss;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.model.BossRecruitAddressModels.AddressRequest;
import com.kuaima.app.domain.boss.model.BossRecruitAddressModels.AddressView;
import com.kuaima.app.domain.boss.service.BossRecruitAddressService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

@RestController
@RequestMapping("/boss/recruit-addresses")
public class BossRecruitAddressController {
    private final BossRecruitAddressService service;
    public BossRecruitAddressController(BossRecruitAddressService service) { this.service = service; }
    @GetMapping public Result<List<AddressView>> list(Authentication a) { return Result.success(service.list(bossId(a))); }
    @PostMapping public Result<AddressView> create(@RequestBody AddressRequest r, Authentication a) { return Result.success(service.create(bossId(a), r)); }
    @PutMapping("/{id}") public Result<AddressView> update(@PathVariable Long id, @RequestBody AddressRequest r, Authentication a) { return Result.success(service.update(bossId(a), id, r)); }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id, Authentication a) { service.delete(bossId(a), id); return Result.success(); }
    @PutMapping("/{id}/default") public Result<AddressView> setDefault(@PathVariable Long id, Authentication a) { return Result.success(service.setDefault(bossId(a), id)); }
    private Long bossId(Authentication a) { if (a != null && a.getPrincipal() instanceof LoginUser u && UserRole.BOSS.equals(u.role()) && u.id() != null) return u.id(); throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号"); }
}
