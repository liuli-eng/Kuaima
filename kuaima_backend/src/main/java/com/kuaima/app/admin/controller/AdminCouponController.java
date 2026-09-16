package com.kuaima.app.admin.controller;

import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kuaima.app.admin.service.AdminCouponService;
import com.kuaima.app.common.*;
import com.kuaima.app.security.model.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/admin/coupons") @Tag(name="后台-优惠券管理")
public class AdminCouponController {
 private final AdminCouponService service; public AdminCouponController(AdminCouponService s){service=s;}
 @GetMapping @Operation(summary="优惠券分页查询") public Result<List<Map<String,Object>>> list(@RequestParam(required=false)String status,@RequestParam(required=false)String type,@RequestParam(required=false)String target,@RequestParam(required=false)String keyword,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="10")int size,Authentication a){admin(a,false);if(page<0||size<1||size>200)throw new IllegalArgumentException("page 或 size 参数无效");var p=service.list(status,type,target,keyword,PageRequest.of(page,size));return Result.success(p.stream().map(service::view).toList(),page,p.getTotalElements());}
 @GetMapping("/stats") public Result<Map<String,Object>> stats(Authentication a){admin(a,false);return Result.success(service.stats());}
 @PostMapping public Result<Map<String,Object>> create(@RequestBody Map<String,Object>b,Authentication a){admin(a,true);return Result.success(service.view(service.create(b)));}
 @PutMapping("/{id}") public Result<Map<String,Object>> update(@PathVariable Long id,@RequestBody Map<String,Object>b,Authentication a){admin(a,true);return Result.success(service.view(service.update(id,b)));}
 @PutMapping("/{id}/status/toggle") public Result<Map<String,Object>> toggle(@PathVariable Long id,Authentication a){admin(a,true);return Result.success(service.view(service.toggle(id)));}
 @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id,Authentication a){admin(a,true);service.delete(id);return Result.success();}
 private LoginUser admin(Authentication a,boolean write){if(a==null||!(a.getPrincipal() instanceof LoginUser u)||u.role()==null||!u.role().startsWith("ADMIN_"))throw new ForbiddenBusinessException("仅管理员可操作");if(write&&u.role().endsWith("VIEWER"))throw new ForbiddenBusinessException("当前管理员无优惠券管理权限");return u;}
}
