package com.kuaima.app.domain.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.admin.entity.Certification;
import com.kuaima.app.admin.repository.CertificationRepository;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.user.constant.CertificationStatus;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CertificationService {

    public static final String REALNAME = "REALNAME";
    public static final String ENTERPRISE = "ENTERPRISE";

    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;

    public CertificationService(UserRepository userRepository,
                                CertificationRepository certificationRepository) {
        this.userRepository = userRepository;
        this.certificationRepository = certificationRepository;
    }

    @Transactional
    public User submitRealname(Long userId, String realName, String idCard) {
        if (!StringUtils.hasText(realName)) throw new IllegalArgumentException("真实姓名不能为空");
        if (!StringUtils.hasText(idCard)) throw new IllegalArgumentException("身份证号不能为空");
        User user = getUser(userId);
        user.setRealName(realName.trim());
        user.setIdCard(idCard.trim());
        user.setRealnameStatus(CertificationStatus.PENDING);
        user.setCertType(REALNAME);
        user.setCertStatus("待审核");
        saveRecord(user, REALNAME, realName, user.getPhone());
        return userRepository.save(user);
    }

    @Transactional
    public User submitEnterprise(Long userId, String companyName, String industry,
                                 String licenseNo, String legalRep) {
        User user = getUser(userId);
        if (!StringUtils.hasText(companyName)) throw new IllegalArgumentException("企业名称不能为空");
        if (!StringUtils.hasText(licenseNo)) throw new IllegalArgumentException("营业执照号不能为空");
        user.setCompanyName(companyName.trim());
        user.setIndustry(StringUtils.hasText(industry) ? industry.trim() : user.getIndustry());
        user.setLicenseNo(licenseNo.trim());
        user.setLegalRep(StringUtils.hasText(legalRep) ? legalRep.trim() : user.getLegalRep());
        user.setEnterpriseStatus(CertificationStatus.PENDING);
        user.setCertType(ENTERPRISE);
        user.setCertStatus("待审核");
        saveRecord(user, ENTERPRISE, companyName, user.getContactPhone() != null ? user.getContactPhone() : user.getPhone());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> publishEligibility(Long userId) {
        User user = getUser(userId);
        String realname = resolveStatus(user, REALNAME, user.getRealnameStatus());
        String enterprise = resolveStatus(user, ENTERPRISE, user.getEnterpriseStatus());
        List<String> missing = new ArrayList<>();
        if (!CertificationStatus.APPROVED.equals(realname)) missing.add("REALNAME");
        if (!CertificationStatus.APPROVED.equals(enterprise)) missing.add("ENTERPRISE");
        return Map.of("canPublish", missing.isEmpty(),
                "realnameStatus", realname,
                "enterpriseStatus", enterprise,
                "missing", missing);
    }

    public void requirePublishEligibility(Long userId) {
        Map<String, Object> result = publishEligibility(userId);
        if (!Boolean.TRUE.equals(result.get("canPublish"))) {
            throw new ForbiddenBusinessException("请先完成个人实名认证和企业认证");
        }
    }

    @Transactional
    public Certification audit(Long certificationId, boolean approved, String reason) {
        Certification record = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new EntityNotFoundException("认证记录不存在: " + certificationId));
        String status = approved ? "已通过" : "已拒绝";
        record.setStatus(status);
        record.setRejectReason(approved ? null : reason);
        record.setAuditTime(LocalDateTime.now());
        User user = getUser(record.getUserId());
        String type = record.getType();
        boolean realname = REALNAME.equalsIgnoreCase(type) || "零工实名".equals(type);
        if (realname) {
            user.setRealnameStatus(approved ? CertificationStatus.APPROVED : CertificationStatus.REJECTED);
        } else if (ENTERPRISE.equalsIgnoreCase(type) || "企业认证".equals(type)) {
            user.setEnterpriseStatus(approved ? CertificationStatus.APPROVED : CertificationStatus.REJECTED);
        }
        user.setCertType(type);
        user.setCertStatus(status);
        userRepository.save(user);
        return certificationRepository.save(record);
    }

    private void saveRecord(User user, String type, String applicantName, String phone) {
        Certification record = new Certification();
        record.setUserId(user.getId());
        record.setType(type);
        record.setApplicantName(applicantName);
        record.setContactPhone(phone);
        record.setStatus("待审核");
        record.setApplyTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        certificationRepository.save(record);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
    }

    private String normalizedStatus(String status) {
        if (!StringUtils.hasText(status)) return CertificationStatus.UNVERIFIED;
        return switch (status) {
            case "待审核" -> CertificationStatus.PENDING;
            case "已通过" -> CertificationStatus.APPROVED;
            case "已拒绝" -> CertificationStatus.REJECTED;
            default -> status;
        };
    }

    private String resolveStatus(User user, String type, String current) {
        if (StringUtils.hasText(current) && !CertificationStatus.UNVERIFIED.equals(current)) {
            return normalizedStatus(current);
        }
        return certificationRepository.findTopByUserIdAndTypeOrderByIdDesc(user.getId(), type)
                .map(record -> normalizedStatus(record.getStatus()))
                .orElseGet(() -> type.equalsIgnoreCase(user.getCertType())
                        ? normalizedStatus(user.getCertStatus())
                        : CertificationStatus.UNVERIFIED);
    }
}
