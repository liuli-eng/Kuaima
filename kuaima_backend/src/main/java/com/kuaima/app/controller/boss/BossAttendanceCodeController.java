package com.kuaima.app.controller.boss;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.service.BossAttendanceCodeService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
import com.kuaima.app.domain.enterprise.service.EnterpriseContextService;
@RestController @RequestMapping("/boss/attendance-codes")
public class BossAttendanceCodeController {
 private final BossAttendanceCodeService service;
 private final EnterpriseContextService contexts;
 public BossAttendanceCodeController(BossAttendanceCodeService service){this(service,null);}
 @org.springframework.beans.factory.annotation.Autowired public BossAttendanceCodeController(BossAttendanceCodeService service,EnterpriseContextService contexts){this.service=service;this.contexts=contexts;}
 @GetMapping public Result<Map<String,Object>> today(Authentication a){
   LoginUser u=bossPrincipal(a);
   if (u.enterpriseId() == null || contexts == null) return Result.success(service.today(u.id()));
   var c=contexts.require(a,"SETTINGS_VIEW");
   return Result.success(service.todayByEnterprise(c.enterprise().getId(),c.user().getId()));
 }
 @PostMapping("/work/refresh") public Result<Map<String,Object>> work(Authentication a){
   LoginUser u=bossPrincipal(a);
   if (u.enterpriseId() == null || contexts == null) return Result.success(service.refresh(u.id(),true));
   var c=contexts.require(a,"SETTINGS_WRITE");
   return Result.success(service.refreshByEnterprise(c.enterprise().getId(),c.user().getId(),true));
 }
 @PostMapping("/leave/refresh") public Result<Map<String,Object>> leave(Authentication a){
   LoginUser u=bossPrincipal(a);
   if (u.enterpriseId() == null || contexts == null) return Result.success(service.refresh(u.id(),false));
   var c=contexts.require(a,"SETTINGS_WRITE");
   return Result.success(service.refreshByEnterprise(c.enterprise().getId(),c.user().getId(),false));
 }
 private LoginUser bossPrincipal(Authentication a){if(a!=null&&a.getPrincipal() instanceof LoginUser u&&u.id()!=null&&UserRole.BOSS.equals(u.role()))return u;throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");}
 private Long boss(Authentication a){if(a!=null&&a.getPrincipal() instanceof LoginUser u&&u.id()!=null&&UserRole.BOSS.equals(u.role()))return u.id();throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");}
}
