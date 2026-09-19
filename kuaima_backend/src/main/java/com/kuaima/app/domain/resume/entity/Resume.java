package com.kuaima.app.domain.resume.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 老板端简历库中的简历。来源：工人投递(APPLY)或文件导入(IMPORT)。 */
@Entity
@Table(name = "boss_resume", indexes = {
        @Index(name = "idx_resume_boss_status", columnList = "boss_id,status"),
        @Index(name = "idx_resume_category", columnList = "job_category")
})
@Getter
@Setter
public class Resume extends BaseEntity {

    @Column(name = "boss_id", nullable = false)
    private Long bossId;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender;

    private Integer age;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String city;

    @Column(length = 30)
    private String idCard;

    /** 意向岗位，如：分拣打包员 */
    @Column(length = 50)
    private String position;

    /** 职位类型，如：分拣打包/搬运装卸/餐饮服务 */
    @Column(name = "job_category", length = 30)
    private String jobCategory;

    /** 学历，如：初中及以上/高中中专/大专及以上/不限 */
    @Column(length = 20)
    private String education;

    /** 经验描述，如：2年经验/应届 */
    @Column(length = 30)
    private String experience;

    /** 期望薪资，如：180-260元/天 */
    @Column(name = "expected_salary", length = 50)
    private String expectedSalary;

    @Column(name = "work_location", length = 50)
    private String workLocation;

    /** NEW 新 / PENDING 待处理 / VIEWED 已查看 / SENT 已投递 */
    @Column(nullable = false, length = 20)
    private String status = "NEW";

    @Column(nullable = false)
    private Boolean favorite = false;

    /** APPLY 投递 / IMPORT 导入 */
    @Column(nullable = false, length = 20)
    private String source = "APPLY";

    @Column(name = "file_url", length = 500)
    private String fileUrl;
}
