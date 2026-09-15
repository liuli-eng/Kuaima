package com.kuaima.app.controller.boss;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.service.BossAttendanceCodeService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
@RestController @RequestMapping("/boss/attendance-codes")
public class BossAttendanceCodeController {
 private final BossAttendanceCodeService service;
 public BossAttendanceCodeController(BossAttendanceCodeService service){this.service=service;}
 @GetMapping public Result<Map<String,Object>> today(Authentication a){return Result.success(service.today(boss(a)));}
 @PostMapping("/work/refresh") public Result<Map<String,Object>> work(Authentication a){return Result.success(service.refresh(boss(a),true));}
 @PostMapping("/leave/refresh") public Result<Map<String,Object>> leave(Authentication a){return Result.success(service.refresh(boss(a),false));}
 private Long boss(Authentication a){if(a!=null&&a.getPrincipal() instanceof LoginUser u&&u.id()!=null&&UserRole.BOSS.equals(u.role()))return u.id();throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");}
}
