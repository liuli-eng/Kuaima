package com.kuaima.app.controller.boss;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.kuaima.app.security.model.LoginUser;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossAddress;
import com.kuaima.app.domain.boss.repository.BossAddressRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * 老板常用招工地址：列表/新增/删除/设为默认（设为默认时清空该老板其它默认标记）。
 */
@RestController
@RequestMapping("/boss/addresses")
@RequiredArgsConstructor
@Tag(name = "老板-地址", description = "老板工作地址管理")
public class BossAddressController {

    private final BossAddressRepository bossAddressRepository;

    /** 某老板的全部地址（最新在前） */
    @GetMapping
    public Result<List<BossAddress>> list(@RequestParam Long userId, Authentication authentication) {
        requireOwner(userId, authentication);
        return Result.success(bossAddressRepository.findByUserIdOrderByIdDesc(userId));
    }

    /** 新增地址（body 为 BossAddress 字段，userId 必填） */
    @PostMapping
    @Transactional
    public Result<BossAddress> create(@RequestBody BossAddress body, Authentication authentication) {
        if (body.getUserId() == null) {
            return Result.error("userId 不能为空");
        }
        requireOwner(body.getUserId(), authentication);
        if (Boolean.TRUE.equals(body.getIsDefault())) {
            bossAddressRepository.clearDefaultByUserId(body.getUserId());
        }
        return Result.success(bossAddressRepository.save(body));
    }

    /** 编辑地址，仅允许所属老板修改。 */
    @PutMapping("/{id}")
    @Transactional
    public Result<BossAddress> update(@PathVariable Long id, @RequestBody BossAddress body,
                                     Authentication authentication) {
        BossAddress existing = ownedAddress(id, authentication);
        if (body.getName() != null) existing.setName(body.getName());
        if (body.getDetail() != null) existing.setDetail(body.getDetail());
        if (body.getLat() != null) existing.setLat(body.getLat());
        if (body.getLng() != null) existing.setLng(body.getLng());
        if (body.getIsDefault() != null) {
            if (body.getIsDefault()) bossAddressRepository.clearDefaultByUserId(existing.getUserId());
            existing.setIsDefault(body.getIsDefault());
        }
        return Result.success(bossAddressRepository.save(existing));
    }

    /** 删除地址 */
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        BossAddress address = ownedAddress(id, authentication);
        bossAddressRepository.delete(address);
        return Result.success();
    }

    @GetMapping("/default")
    public Result<BossAddress> getDefault(@RequestParam Long userId, Authentication authentication) {
        requireOwner(userId, authentication);
        return Result.success(bossAddressRepository.findByUserIdAndIsDefaultTrue(userId).orElse(null));
    }

    /** 选择当前使用地址：与现有默认地址语义统一，不增加第二套 currentAddress 状态。 */
    @PutMapping("/{id}/use")
    @Transactional
    public Result<BossAddress> use(@PathVariable Long id, Authentication authentication) {
        BossAddress address = ownedAddress(id, authentication);
        bossAddressRepository.clearDefaultByUserId(address.getUserId());
        address.setIsDefault(true);
        return Result.success(bossAddressRepository.save(address));
    }

    /** 在老板已保存地址中按名称/详情搜索；不代替地图 POI 搜索。 */
    @GetMapping("/search")
    public Result<List<BossAddress>> search(@RequestParam Long userId, @RequestParam String keyword,
                                            Authentication authentication) {
        requireOwner(userId, authentication);
        if (keyword == null || keyword.isBlank()) throw new IllegalArgumentException("keyword 不能为空");
        String k = keyword.trim().toLowerCase();
        return Result.success(bossAddressRepository.findByUserIdOrderByIdDesc(userId).stream()
                .filter(a -> (a.getName() != null && a.getName().toLowerCase().contains(k))
                        || (a.getDetail() != null && a.getDetail().toLowerCase().contains(k)))
                .toList());
    }

    /** 设为默认：先清空该老板其它默认标记，再置当前为默认 */
    @PutMapping("/{id}/default")
    @Transactional
    public Result<BossAddress> setDefault(@PathVariable Long id, Authentication authentication) {
        BossAddress address = ownedAddress(id, authentication);
        bossAddressRepository.clearDefaultByUserId(address.getUserId());
        address.setIsDefault(true);
        return Result.success(bossAddressRepository.save(address));
    }

    private BossAddress ownedAddress(Long id, Authentication authentication) {
        BossAddress address = bossAddressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在: " + id));
        requireOwner(address.getUserId(), authentication);
        return address;
    }

    private void requireOwner(Long userId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)
                || loginUser.id() == null) throw new IllegalArgumentException("无法获取当前登录用户");
        if (!loginUser.id().equals(userId)) throw new com.kuaima.app.common.ForbiddenBusinessException("无权访问其他老板的地址");
    }
}
