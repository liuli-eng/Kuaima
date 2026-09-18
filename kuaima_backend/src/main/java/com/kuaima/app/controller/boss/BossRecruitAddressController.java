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
import com.kuaima.app.domain.enterprise.service.EnterpriseContextService;

@RestController
@RequestMapping("/boss/recruit-addresses")
public class BossRecruitAddressController {
    private final BossRecruitAddressService service;
    private final EnterpriseContextService contexts;
    public BossRecruitAddressController(BossRecruitAddressService service) { this(service, null); }
    @org.springframework.beans.factory.annotation.Autowired public BossRecruitAddressController(BossRecruitAddressService service, EnterpriseContextService contexts) { this.service = service; this.contexts = contexts; }
    @GetMapping public Result<List<AddressView>> list(Authentication a) { var c = context(a, "ADDRESS_VIEW"); return Result.success(contexts == null ? service.list(bossId(a)) : service.listByEnterprise(c.enterprise().getId())); }
    @PostMapping public Result<AddressView> create(@RequestBody AddressRequest r, Authentication a) { var c = context(a, "ADDRESS_WRITE"); return Result.success(contexts == null ? service.create(bossId(a), r) : service.createByEnterprise(c.enterprise().getId(), c.user().getId(), r)); }
    @PutMapping("/{id}") public Result<AddressView> update(@PathVariable Long id, @RequestBody AddressRequest r, Authentication a) { var c = context(a, "ADDRESS_WRITE"); return Result.success(contexts == null ? service.update(bossId(a), id, r) : service.updateByEnterprise(c.enterprise().getId(), id, r)); }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id, Authentication a) { var c = context(a, "ADDRESS_WRITE"); if (contexts == null) service.delete(bossId(a), id); else service.deleteByEnterprise(c.enterprise().getId(), id); return Result.success(); }
    @PutMapping("/{id}/default") public Result<AddressView> setDefault(@PathVariable Long id, Authentication a) { var c = context(a, "ADDRESS_WRITE"); return Result.success(contexts == null ? service.setDefault(bossId(a), id) : service.setDefaultByEnterprise(c.enterprise().getId(), id)); }
    private EnterpriseContextService.Context context(Authentication a, String p) { return contexts == null ? null : contexts.require(a, p); }
    private Long bossId(Authentication a) { if (a != null && a.getPrincipal() instanceof LoginUser u && UserRole.BOSS.equals(u.role()) && u.id() != null) return u.id(); throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号"); }
}
