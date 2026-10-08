package com.kuaima.app.domain.payroll.model;

import java.util.List;

import com.kuaima.app.domain.payroll.entity.PayrollDetail;

import lombok.Getter;
import lombok.Setter;

/** 老板端「新建批量发薪」请求：发薪单基础信息 + 成员明细（日薪按「分」）。 */
@Getter
@Setter
public class BossPayrollCreateRequest {

    private String title;

    private Long projectId;

    private String projectName;

    /** wage工资 / advance预支 / other其他 */
    private String type;

    private List<PayrollDetail> details;
}