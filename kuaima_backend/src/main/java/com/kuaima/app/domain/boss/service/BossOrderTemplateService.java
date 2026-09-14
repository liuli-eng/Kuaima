package com.kuaima.app.domain.boss.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.entity.BossOrderTemplate;
import com.kuaima.app.domain.boss.model.BossOrderTemplateModels.CreateRequest;
import com.kuaima.app.domain.boss.model.BossOrderTemplateModels.TemplateView;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.repository.BossOrderTemplateRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BossOrderTemplateService {
    private final BossOrderTemplateRepository templateRepository;
    private final BossOrderRespository orderRepository;

    public BossOrderTemplateService(BossOrderTemplateRepository templateRepository,
                                    BossOrderRespository orderRepository) {
        this.templateRepository = templateRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public BossOrderTemplate create(Long bossId, Long orderId, CreateRequest request) {
        BossOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在"));
        if (!bossId.equals(order.getCreateBy())) throw new ForbiddenBusinessException("无权操作该订单");
        String name = validName(request == null ? null : request.templateName());
        BossOrderTemplate template = templateRepository.findByOwnerUserIdAndTemplateName(bossId, name)
                .map(existing -> {
                    if (!Boolean.TRUE.equals(request.overwrite())) throw new IllegalArgumentException("模板名称已存在");
                    return existing;
                }).orElseGet(BossOrderTemplate::new);
        template.setOwnerUserId(bossId);
        template.setTemplateName(name);
        copyOrder(template, order);
        return templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public Page<BossOrderTemplate> list(Long bossId, int page, int size) {
        return templateRepository.findByOwnerUserIdOrderByIdDesc(bossId,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)));
    }

    @Transactional(readOnly = true)
    public TemplateView detail(Long bossId, Long id) {
        BossOrderTemplate template = owned(bossId, id);
        BossOrder source = template.getSourceOrderId() == null ? null
                : orderRepository.findById(template.getSourceOrderId()).orElse(null);
        return toView(template, source);
    }

    @Transactional
    public TemplateView rename(Long bossId, Long id, String templateName) {
        BossOrderTemplate template = owned(bossId, id);
        String name = validName(templateName);
        if (templateRepository.existsByOwnerUserIdAndTemplateNameAndIdNot(bossId, name, id)) {
            throw new IllegalArgumentException("模板名称已存在");
        }
        template.setTemplateName(name);
        BossOrderTemplate saved = templateRepository.save(template);
        BossOrder source = saved.getSourceOrderId() == null ? null
                : orderRepository.findById(saved.getSourceOrderId()).orElse(null);
        return toView(saved, source);
    }

    @Transactional
    public void delete(Long bossId, Long id) {
        templateRepository.delete(owned(bossId, id));
    }

    private BossOrderTemplate owned(Long bossId, Long id) {
        BossOrderTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("模板不存在"));
        if (!bossId.equals(template.getOwnerUserId())) throw new ForbiddenBusinessException("无权操作该模板");
        return template;
    }

    private String validName(String value) {
        if (!StringUtils.hasText(value)) throw new IllegalArgumentException("模板名称不能为空");
        String name = value.trim();
        if (name.length() > 100) throw new IllegalArgumentException("模板名称不能超过100个字符");
        return name;
    }

    private void copyOrder(BossOrderTemplate template, BossOrder order) {
        template.setSourceOrderId(order.getId());
        template.setOrderTitle(order.getOrderTitle());
        template.setPositionName(order.getPostion());
        template.setSalaryAmount(order.getSalary());
        template.setSalaryUnit(order.getType());
        template.setDuration(order.getDuration());
        template.setWorkDate(order.getStartTime());
        template.setStartTime(order.getStartTime());
        template.setEndTime(order.getEndTime());
        template.setAddress(order.getAddress());
        template.setRecruitCount(order.getOrderNum());
        template.setGenderRequirement(order.getGender());
        template.setExperienceRequirement(order.getExperience());
        template.setTags(order.getTags());
    }

    private TemplateView toView(BossOrderTemplate template, BossOrder source) {
        return new TemplateView(template.getId(), template.getTemplateName(), template.getSourceOrderId(),
                source != null ? source.getOrderTitle() : template.getOrderTitle(),
                source != null ? source.getType() : template.getSalaryUnit(),
                source != null ? source.getPostion() : template.getPositionName(),
                source != null ? source.getSalary() : template.getSalaryAmount(),
                source != null ? source.getDuration() : template.getDuration(),
                source != null ? source.getOrderNum() : template.getRecruitCount(),
                source != null ? source.getStartTime() : template.getStartTime(),
                source != null ? source.getEndTime() : template.getEndTime(),
                source != null ? source.getTags() : template.getTags(),
                source != null ? source.getExperience() : template.getExperienceRequirement(),
                source != null ? source.getGender() : template.getGenderRequirement());
    }
}
