package com.kuaima.app.domain.reward.service;

/** 微信商家转账出站端口，便于服务层隔离微信 API 细节并测试失败退款。 */
public interface WechatMerchantTransferClient {
    MerchantTransferResult transfer(MerchantTransferRequest request);

    MerchantTransferStatus query(String outBatchNo);

    record MerchantTransferRequest(
            String outBatchNo,
            String outDetailNo,
            long amount,
            String openid,
            String remark) {
    }

    record MerchantTransferResult(
            String outBatchNo,
            String batchId,
            String createTime,
            String batchStatus,
            String rawResponse) {
    }

    record MerchantTransferStatus(
            String batchStatus,
            String detailStatus,
            String detailId,
            String rawResponse) {
    }
}
