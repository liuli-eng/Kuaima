package com.kuaima.app.controller.user;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kuaima.app.common.*;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.service.BossOrderService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

/** 零工报名条目兼容接口。新调用应只依赖 JWT，userId 仅用于兼容旧客户端且必须与 JWT 一致。 */
@RestController
@RequestMapping("/worker/items")
public class WorkerItemController {
    private final BossOrderService orderService;
    public WorkerItemController(BossOrderService orderService) { this.orderService = orderService; }

    @GetMapping
    public Result<List<BaseOrderItem>> list(@RequestParam(required = false) Long userId, Authentication authentication) {
        Long current = worker(authentication);
        if (userId != null && !current.equals(userId)) {
            throw new ForbiddenBusinessException("只能查询当前登录零工的报名记录");
        }
        return Result.success(orderService.listItemsByUser(current));
    }

    @PutMapping("/{id}/cancel")
    public Result<BaseOrderItem> cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
                                        Authentication authentication) {
        return Result.success(orderService.cancelWorkerItem(id, worker(authentication), reason));
    }

    private Long worker(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser u
                && u.id() != null && UserRole.USER.equals(u.role())) return u.id();
        throw new ForbiddenBusinessException("当前登录账号不是零工账号");
    }
}
