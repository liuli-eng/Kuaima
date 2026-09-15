package com.kuaima.app.controller.user;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.service.WorkerAttendanceService;
import com.kuaima.app.security.model.LoginUser;
@RestController @RequestMapping("/worker/orders")
public class WorkerAttendanceController {
 private final WorkerAttendanceService service;
 public WorkerAttendanceController(WorkerAttendanceService service){this.service=service;}
 @PostMapping("/{orderId}/check-in") public Result<Map<String,Object>> checkIn(@PathVariable Long orderId,@RequestBody Map<String,String> body,Authentication a){return Result.success(service.checkIn(worker(a),orderId,body==null?null:body.get("code")));}
 @PostMapping("/{orderId}/early-leave") public Result<Map<String,Object>> earlyLeave(@PathVariable Long orderId,@RequestBody Map<String,String> body,Authentication a){return Result.success(service.earlyLeave(worker(a),orderId,body==null?null:body.get("code")));}
 private Long worker(Authentication a){if(a!=null&&a.getPrincipal() instanceof LoginUser u&&u.id()!=null&&UserRole.USER.equals(u.role()))return u.id();throw new ForbiddenBusinessException("当前登录账号不是零工账号");}
}
