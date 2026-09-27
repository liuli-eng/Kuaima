package com.kuaima.app.domain.expense.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseApplicationRequest {
    @NotBlank(message = "报销类型不能为空")
    private String type;

    @NotNull(message = "orderId 不能为空")
    private Long orderId;

    @NotNull(message = "报销金额不能为空")
    @DecimalMin(value = "0.01", message = "报销金额必须大于0")
    @Digits(integer = 16, fraction = 2, message = "报销金额最多两位小数")
    private BigDecimal amount;

    @NotBlank(message = "报销事由不能为空")
    @Size(max = 300, message = "报销事由不能超过300字")
    private String reason;

    @Size(max = 6, message = "凭证最多上传6个")
    private List<@NotBlank(message = "凭证地址不能为空") @Size(max = 1000, message = "凭证地址过长") String> attachments;

    @Size(max = 128, message = "幂等键不能超过128个字符")
    private String idempotencyKey;
}
