package com.kuaima.app.wechat.service;

import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.domain.reward.service.WechatMerchantTransferClient;
import com.kuaima.app.wechat.config.WechatPayProperties;
import com.kuaima.app.wechat.config.WechatProperties;

/** 微信商家转账到零钱 v3 客户端。不在日志或异常中输出 openid、密钥和商户证书信息。 */
@Service
public class WechatMerchantTransferClientImpl implements WechatMerchantTransferClient {
    private static final String TRANSFER_BATCHES_PATH = "/v3/transfer/batches";

    private final WechatPayService payService;
    private final WechatProperties wechat;
    private final WechatPayProperties payProperties;

    public WechatMerchantTransferClientImpl(WechatPayService payService, WechatProperties wechat,
            WechatPayProperties payProperties) {
        this.payService = payService;
        this.wechat = wechat;
        this.payProperties = payProperties;
    }

    @Override
    public MerchantTransferResult transfer(MerchantTransferRequest request) {
        if (request == null || !StringUtils.hasText(request.outBatchNo()) || !StringUtils.hasText(request.outDetailNo())
                || !StringUtils.hasText(request.openid()) || request.amount() <= 0) {
            throw new IllegalArgumentException("微信商家转账参数无效");
        }
        JSONObject detail = new JSONObject();
        detail.put("out_detail_no", request.outDetailNo());
        detail.put("transfer_amount", request.amount());
        detail.put("transfer_remark", StringUtils.hasText(request.remark()) ? request.remark() : "奖励金提现");
        detail.put("openid", request.openid());

        JSONObject body = new JSONObject();
        body.put("appid", wechat.getAppid());
        body.put("out_batch_no", request.outBatchNo());
        body.put("batch_name", "快马日结奖励金提现");
        body.put("batch_remark", "奖励金提现");
        body.put("total_amount", request.amount());
        body.put("total_num", 1);
        body.put("transfer_detail_list", List.of(detail));

        JSONObject response = payService.signedPost(
                TRANSFER_BATCHES_PATH, body, payProperties.getPlatformPublicKeyId());
        String batchId = response.getString("batch_id");
        if (!StringUtils.hasText(batchId)) throw new IllegalStateException("微信商家转账未返回批次单号");
        return new MerchantTransferResult(
                response.getString("out_batch_no"), batchId, response.getString("create_time"),
                response.getString("batch_status"), response.toJSONString());
    }

    @Override
    public MerchantTransferStatus query(String outBatchNo) {
        if (!StringUtils.hasText(outBatchNo)) throw new IllegalArgumentException("微信商家转账批次号不能为空");
        String encodedBatchNo = URLEncoder.encode(outBatchNo, StandardCharsets.UTF_8);
        JSONObject response = payService.signedGet(
                TRANSFER_BATCHES_PATH + "/out-batch-no/" + encodedBatchNo + "?need_query_detail=true&detail_status=ALL");
        JSONObject batch = response.getJSONObject("transfer_batch");
        if (batch == null) throw new IllegalStateException("微信商家转账查询未返回批次信息");
        var details = response.getJSONArray("transfer_detail_list");
        String detailStatus = details == null || details.isEmpty() ? null : details.getJSONObject(0).getString("detail_status");
        String detailId = details == null || details.isEmpty() ? null : details.getJSONObject(0).getString("detail_id");
        return new MerchantTransferStatus(batch.getString("batch_status"), detailStatus, detailId, response.toJSONString());
    }
}
