package com.kuaima.app.controller.chat;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.chat.entity.MissedCall;
import com.kuaima.app.domain.chat.repository.MissedCallRepository;

import lombok.RequiredArgsConstructor;

/**
 * 未接来电：BOSS/USER 拨打后对方未接，前端轮询拉取列表并标记已读。
 */
@RestController
@RequestMapping("/missed-calls")
@RequiredArgsConstructor
public class MissedCallController {

    private final MissedCallRepository missedCallRepository;

    /** 未接来电列表（按通话时间倒序分页） */
    @GetMapping
    public Result<List<MissedCall>> list(@RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MissedCall> result = missedCallRepository
                .findByToUserIdOrderByCallTimeDesc(userId, PageRequest.of(Math.max(page, 0), size));
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 单条标记已读 */
    @PutMapping("/{id}/read")
    @Transactional
    public Result<Boolean> read(@PathVariable Long id) {
        return missedCallRepository.findById(id).map(call -> {
            call.setIsRead(true);
            missedCallRepository.save(call);
            return Result.success(true);
        }).orElse(Result.error(404, "未接来电记录不存在"));
    }
}
