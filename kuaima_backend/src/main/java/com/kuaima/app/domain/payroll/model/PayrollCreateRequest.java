package com.kuaima.app.domain.payroll.model;

import java.util.List;

import com.kuaima.app.domain.payroll.entity.PayrollDetail;
import com.kuaima.app.domain.payroll.entity.PayrollOrder;

import lombok.Getter;
import lombok.Setter;

/** 创建发薪单请求（发薪单 + 人员明细）。 */
@Getter
@Setter
public class PayrollCreateRequest {

    private PayrollOrder order;

    private List<PayrollDetail> details;
}
