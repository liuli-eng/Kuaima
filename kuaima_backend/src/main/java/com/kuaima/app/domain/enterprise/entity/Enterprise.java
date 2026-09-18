package com.kuaima.app.domain.enterprise.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 企业主体。企业认证归属于企业，而不是某一次登录会话。 */
@Entity
@Table(name = "enterprise")
@Getter
@Setter
public class Enterprise extends BaseEntity {
    @Column(name = "company_code", unique = true, nullable = false, length = 32)
    private String companyCode;
    @Column(name = "company_name", nullable = false, length = 120)
    private String companyName;
    @Column(name = "license_no", length = 50)
    private String licenseNo;
    @Column(name = "legal_rep", length = 50)
    private String legalRep;
    @Column(length = 50)
    private String industry;
    @Column(length = 20)
    private String status = "ACTIVE";
}
