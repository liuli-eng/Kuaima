package com.kuaima.app.controller.service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.social.entity.SocialGroup;
import com.kuaima.app.domain.social.repository.SocialGroupRepository;

/**
 * 社群服务（群列表/二维码）。
 */
@RestController
@RequestMapping("/social-groups")
public class SocialGroupController {

    private final SocialGroupRepository socialGroupRepository;

    public SocialGroupController(SocialGroupRepository socialGroupRepository) {
        this.socialGroupRepository = socialGroupRepository;
    }

    /** 社群列表：GET /social-groups */
    @GetMapping
    public Result<List<SocialGroup>> listGroups() {
        return Result.success(socialGroupRepository.findAll());
    }
}
