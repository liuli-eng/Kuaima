package com.kuaima.app.controller.boss;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossOrderTemplate;
import com.kuaima.app.domain.boss.model.BossOrderTemplateModels.CreateRequest;
import com.kuaima.app.domain.boss.model.BossOrderTemplateModels.RenameRequest;
import com.kuaima.app.domain.boss.model.BossOrderTemplateModels.TemplateView;
import com.kuaima.app.domain.boss.service.BossOrderTemplateService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
import com.kuaima.app.domain.enterprise.service.EnterpriseContextService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/order")
@Tag(name = "老板-招工模板", description = "当前JWT老板的招工模板管理")
public class BossOrderTemplateController {
    private final BossOrderTemplateService templateService;
    private final EnterpriseContextService contexts;

    public BossOrderTemplateController(BossOrderTemplateService templateService) {
        this(templateService, null);
    }
    @org.springframework.beans.factory.annotation.Autowired public BossOrderTemplateController(BossOrderTemplateService templateService, EnterpriseContextService contexts) { this.templateService = templateService; this.contexts = contexts; }

    @PostMapping("/{orderId}/template")
    @Operation(summary = "收藏订单为模板", description = "订单和模板归属均取当前JWT老板；overwrite=true时允许覆盖同名模板")
    public Result<BossOrderTemplate> create(@PathVariable Long orderId,
                                            @RequestBody CreateRequest request,
                                            Authentication authentication) {
        var c = context(authentication, "ORDER_CREATE"); return Result.success(contexts == null ? templateService.create(requireBossId(authentication), orderId, request) : templateService.createByEnterprise(c.enterprise().getId(), c.user().getId(), orderId, request));
    }

    @GetMapping("/templates")
    @Operation(summary = "模板列表", description = "分页返回当前JWT老板的模板，按ID倒序")
    public Result<java.util.List<TemplateView>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     Authentication authentication) {
        var c = context(authentication, "ORDER_VIEW"); var result = contexts == null ? templateService.list(requireBossId(authentication), page, size) : templateService.listByEnterprise(c.enterprise().getId(), page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    @GetMapping("/templates/{id}")
    @Operation(summary = "模板详情", description = "返回模板ID、名称和关联订单的完整招工字段；不存在404，非当前老板模板403")
    public Result<TemplateView> detail(@PathVariable Long id, Authentication authentication) {
        var c = context(authentication, "ORDER_VIEW"); return Result.success(contexts == null ? templateService.detail(requireBossId(authentication), id) : templateService.detailByEnterprise(c.enterprise().getId(), id));
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "修改模板名称", description = "模板名称不能为空，同一老板模板名称不可重复；不存在404，越权403")
    public Result<TemplateView> rename(@PathVariable Long id,
                                       @RequestBody RenameRequest request,
                                       Authentication authentication) {
        var c = context(authentication, "ORDER_CREATE"); return Result.success(contexts == null ? templateService.rename(requireBossId(authentication), id, request == null ? null : request.templateName()) : templateService.renameByEnterprise(c.enterprise().getId(), id, request == null ? null : request.templateName()));
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "删除模板", description = "仅允许当前JWT老板删除自己的模板")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        var c = context(authentication, "ORDER_CREATE"); if (contexts == null) templateService.delete(requireBossId(authentication), id); else templateService.deleteByEnterprise(c.enterprise().getId(), id);
        return Result.success();
    }

    private EnterpriseContextService.Context context(Authentication authentication, String permission) { return contexts == null ? null : contexts.require(authentication, permission); }

    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.BOSS.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}
