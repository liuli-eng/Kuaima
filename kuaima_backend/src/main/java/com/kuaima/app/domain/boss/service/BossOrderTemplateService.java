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
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.repository.MessageRepository;
import com.alibaba.fastjson2.JSON;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BossOrderTemplateService {
    private final BossOrderTemplateRepository templateRepository;
    private final BossOrderRespository orderRepository;
    private final MessageRepository messageRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public BossOrderTemplateService(BossOrderTemplateRepository templateRepository,
                                    BossOrderRespository orderRepository,
                                    MessageRepository messageRepository) {
        this.templateRepository = templateRepository;
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
    }

    /** 保留已有单元测试及非 Spring 调用构造方式。 */
    BossOrderTemplateService(BossOrderTemplateRepository templateRepository,
                             BossOrderRespository orderRepository) {
        this(templateRepository, orderRepository, null);
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
    public Page<TemplateView> list(Long bossId, int page, int size) {
        return templateRepository.findByOwnerUserIdOrderByIdDesc(bossId,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)))
                .map(template -> toView(template, null));
    }

    @Transactional(readOnly = true)
    public Page<TemplateView> listByEnterprise(Long enterpriseId, int page, int size) {
        return templateRepository.findByEnterpriseIdOrderByIdDesc(enterpriseId,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)))
                .map(template -> toView(template, null));
    }

    @Transactional
    public BossOrderTemplate createByEnterprise(Long enterpriseId, Long operatorId, Long orderId, CreateRequest request) {
        BossOrder order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("订单不存在"));
        if (!java.util.Objects.equals(order.getEnterpriseId(), enterpriseId)
                && !java.util.Objects.equals(order.getCreateBy(), operatorId)) throw new ForbiddenBusinessException("无权操作该订单");
        String name = validName(request == null ? null : request.templateName());
        BossOrderTemplate template = templateRepository.findByEnterpriseIdAndTemplateName(enterpriseId, name).map(existing -> {
            if (!Boolean.TRUE.equals(request.overwrite())) throw new IllegalArgumentException("模板名称已存在"); return existing;
        }).orElseGet(BossOrderTemplate::new);
        template.setEnterpriseId(enterpriseId); template.setOwnerUserId(operatorId); template.setTemplateName(name); copyOrder(template, order);
        return templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public TemplateView detailByEnterprise(Long enterpriseId, Long id) {
        BossOrderTemplate template = templateRepository.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(() -> new ForbiddenBusinessException("无权操作该模板"));
        BossOrder source = template.getSourceOrderId() == null ? null : orderRepository.findById(template.getSourceOrderId()).orElse(null);
        return toView(template, source);
    }

    @Transactional
    public TemplateView renameByEnterprise(Long enterpriseId, Long id, String name) {
        BossOrderTemplate template = templateRepository.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(() -> new ForbiddenBusinessException("无权操作该模板"));
        String valid = validName(name); if (templateRepository.existsByEnterpriseIdAndTemplateNameAndIdNot(enterpriseId, valid, id)) throw new IllegalArgumentException("模板名称已存在");
        template.setTemplateName(valid); BossOrderTemplate saved = templateRepository.save(template); BossOrder source = saved.getSourceOrderId() == null ? null : orderRepository.findById(saved.getSourceOrderId()).orElse(null); return toView(saved, source);
    }

    @Transactional
    public void deleteByEnterprise(Long enterpriseId, Long id) {
        BossOrderTemplate template = templateRepository.findByIdAndEnterpriseId(id, enterpriseId).orElseThrow(() -> new EntityNotFoundException("模板不存在"));
        templateRepository.delete(template);
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
        template.setOrderContent(order.getOrderContent());
        template.setIndustryId(order.getIndustryId()); template.setEnterpriseTypeIds(order.getEnterpriseTypeIds()); template.setJobIds(order.getJobIds()); template.setJobCategoryId(order.getJobCategoryId());
        template.setPositionName(order.getPostion());
        template.setSalaryAmount(order.getSalary());
        template.setSalaryUnit(order.getType());
        template.setDuration(order.getDuration());
        template.setWorkDate(order.getStartTime());
        template.setStartTime(order.getStartTime());
        template.setEndTime(order.getEndTime());
        template.setAddress(order.getAddress());
        template.setLongitude(order.getLongitude());
        template.setLatitude(order.getLatitude());
        template.setRecruitCount(order.getOrderNum());
        template.setGenderRequirement(order.getGender());
        template.setExperienceRequirement(order.getExperience());
        template.setTags(order.getTags());
        template.setSignMode(order.getSignMode()); template.setPhoneNotify(order.getPhoneNotify()); template.setSignNotify(order.getSignNotify()); template.setStartRemind(order.getStartRemind()); template.setSettleNotify(order.getSettleNotify());
        template.setInvitedWorkerIds(JSON.toJSONString(invitedWorkerIds(order.getId())));
    }

    private TemplateView toView(BossOrderTemplate template, BossOrder source) {
        return new TemplateView(template.getId(), template.getTemplateName(), template.getSourceOrderId(),
                source != null ? source.getOrderTitle() : template.getOrderTitle(), source != null ? source.getOrderContent() : template.getOrderContent(),
                source != null ? source.getType() : template.getSalaryUnit(), source != null ? source.getIndustryId() : template.getIndustryId(),
                ids(source != null ? source.getEnterpriseTypeIds() : template.getEnterpriseTypeIds()), ids(source != null ? source.getJobIds() : template.getJobIds()),
                source != null ? source.getJobCategoryId() : template.getJobCategoryId(), source != null ? source.getPostion() : template.getPositionName(),
                source != null ? source.getAddress() : template.getAddress(),
                source != null ? source.getLongitude() : template.getLongitude(),
                source != null ? source.getLatitude() : template.getLatitude(),
                source != null ? source.getSalary() : template.getSalaryAmount(),
                source != null ? source.getDuration() : template.getDuration(),
                source != null ? source.getOrderNum() : template.getRecruitCount(),
                source != null ? source.getStartTime() : template.getStartTime(),
                source != null ? source.getEndTime() : template.getEndTime(),
                source != null ? source.getTags() : template.getTags(),
                source != null ? source.getExperience() : template.getExperienceRequirement(),
                source != null ? source.getGender() : template.getGenderRequirement(), source != null ? source.getSignMode() : template.getSignMode(),
                source != null ? source.getPhoneNotify() : template.getPhoneNotify(), source != null ? source.getSignNotify() : template.getSignNotify(),
                source != null ? source.getStartRemind() : template.getStartRemind(), source != null ? source.getSettleNotify() : template.getSettleNotify(),
                templateInvitedWorkerIds(template, source));
    }
    private java.util.List<Long> ids(String value) { if (!StringUtils.hasText(value)) return java.util.List.of(); return java.util.Arrays.stream(value.replace("[", "").replace("]", "").replace("\"", "").split(",")).map(String::trim).filter(StringUtils::hasText).map(Long::valueOf).toList(); }
    private java.util.List<Long> invitedWorkerIds(Long orderId) {
        if (orderId == null || messageRepository == null) return java.util.List.of();
        return messageRepository.findDistinctUserIdsByTypeAndBizTypeAndBizId(
                MessageType.BOSS_INVITE, BizType.ORDER, orderId);
    }
    private java.util.List<Long> templateInvitedWorkerIds(BossOrderTemplate template, BossOrder source) {
        if (StringUtils.hasText(template.getInvitedWorkerIds())) {
            try { return JSON.parseArray(template.getInvitedWorkerIds(), Long.class); }
            catch (RuntimeException ignored) { return java.util.List.of(); }
        }
        return source == null ? java.util.List.of() : invitedWorkerIds(source.getId());
    }
}
