package com.kuaima.app.controller.boss;

import java.util.List;

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
public class BossAddressController {

    private final BossAddressRepository bossAddressRepository;

    /** 某老板的全部地址（最新在前） */
    @GetMapping
    public Result<List<BossAddress>> list(@RequestParam Long userId) {
        return Result.success(bossAddressRepository.findByUserIdOrderByIdDesc(userId));
    }

    /** 新增地址（body 为 BossAddress 字段，userId 必填） */
    @PostMapping
    @Transactional
    public Result<BossAddress> create(@RequestBody BossAddress body) {
        if (body.getUserId() == null) {
            return Result.error("userId 不能为空");
        }
        if (Boolean.TRUE.equals(body.getIsDefault())) {
            bossAddressRepository.clearDefaultByUserId(body.getUserId());
        }
        return Result.success(bossAddressRepository.save(body));
    }

    /** 删除地址 */
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        bossAddressRepository.deleteById(id);
        return Result.success();
    }

    /** 设为默认：先清空该老板其它默认标记，再置当前为默认 */
    @PutMapping("/{id}/default")
    @Transactional
    public Result<BossAddress> setDefault(@PathVariable Long id) {
        BossAddress address = bossAddressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在: " + id));
        bossAddressRepository.clearDefaultByUserId(address.getUserId());
        address.setIsDefault(true);
        return Result.success(bossAddressRepository.save(address));
    }
}
