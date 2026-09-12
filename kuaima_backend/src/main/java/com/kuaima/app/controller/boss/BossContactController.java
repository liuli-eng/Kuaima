package com.kuaima.app.controller.boss;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
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

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossContact;
import com.kuaima.app.domain.boss.repository.BossContactRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * 老板常用联系人：列表/新增/删除/设为默认（设为默认时清空该老板其它默认标记）。
 */
@RestController
@RequestMapping("/boss/contacts")
@RequiredArgsConstructor
@Tag(name = "老板-联系人", description = "老板常用联系人管理")
public class BossContactController {

    private final BossContactRepository bossContactRepository;

    /** 某老板的全部联系人（最新在前） */
    @Operation(summary = "联系人列表", description = "返回老板全部常用联系人，最新在前")
    @GetMapping
    public Result<List<BossContact>> list(@RequestParam Long userId) {
        return Result.success(bossContactRepository.findByUserIdOrderByIdDesc(userId));
    }

    /** 新增联系人（body 为 BossContact 字段，userId 必填） */
    @Operation(summary = "新增联系人", description = "body 为 BossContact 字段，userId 必填；isDefault=true 时先清空该老板其它默认标记再保存")
    @PostMapping
    @Transactional
    public Result<BossContact> create(@RequestBody BossContact body) {
        if (body.getUserId() == null) {
            return Result.error("userId 不能为空");
        }
        if (Boolean.TRUE.equals(body.getIsDefault())) {
            bossContactRepository.clearDefaultByUserId(body.getUserId());
        }
        return Result.success(bossContactRepository.save(body));
    }

    /** 删除联系人 */
    @Operation(summary = "删除联系人", description = "按 id 删除联系人记录")
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        bossContactRepository.deleteById(id);
        return Result.success();
    }

    /** 设为默认：先清空该老板其它默认标记，再置当前为默认 */
    @Operation(summary = "设为默认联系人", description = "先清空该老板其它默认标记，再置当前联系人为默认；不存在时返回 404")
    @PutMapping("/{id}/default")
    @Transactional
    public Result<BossContact> setDefault(@PathVariable Long id) {
        BossContact contact = bossContactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("联系人不存在: " + id));
        bossContactRepository.clearDefaultByUserId(contact.getUserId());
        contact.setIsDefault(true);
        return Result.success(bossContactRepository.save(contact));
    }
}
