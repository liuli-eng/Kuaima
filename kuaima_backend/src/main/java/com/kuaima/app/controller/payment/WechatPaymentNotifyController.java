package com.kuaima.app.controller.payment;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.domain.points.service.BossPointsService;
import com.kuaima.app.admin.repository.PointPurchaseOrderRepository;
import com.kuaima.app.wechat.service.WechatPayService;
import com.kuaima.app.domain.wallet.service.SettlementService;
import com.kuaima.app.domain.reward.service.RewardRechargeService;
import com.kuaima.app.domain.wallet.service.BossBalancePaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 微信支付 API v3 通知入口。
 *
 * 外部完整地址为： https://ke.taifang.xyz/api/pay/wechat/notify
 * /api 由网关/Nginx 转发前缀提供，Spring 应用内部路径为 /pay/wechat/notify。
 * 在商户号、API v3 Key 和微信平台证书配置完成前，本接口不会确认支付成功。
 */
@RestController
@RequestMapping("/pay/wechat")
@Tag(name = "微信支付", description = "微信支付 API v3 通知回调")
public class WechatPaymentNotifyController {
    private final WechatPayService wechatPay;
    private final BossPointsService points;
    private final PointPurchaseOrderRepository orders;
    private final SettlementService settlements;
    private final RewardRechargeService rewardRecharges;
    private final BossBalancePaymentService bossBalancePayments;
    public WechatPaymentNotifyController(WechatPayService wechatPay, BossPointsService points,
                                         PointPurchaseOrderRepository orders, SettlementService settlements,
                                         RewardRechargeService rewardRecharges,
                                         BossBalancePaymentService bossBalancePayments) {
        this.wechatPay = wechatPay; this.points = points; this.orders = orders; this.settlements = settlements;
        this.rewardRecharges = rewardRecharges;
        this.bossBalancePayments = bossBalancePayments;
    }

    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "微信支付通知回调", description = "微信服务器调用；验签、解密和订单幂等处理完成后才返回 SUCCESS")
    public ResponseEntity<Map<String, String>> notify(
            @RequestHeader HttpHeaders headers,
            @RequestBody String body) {
        try {
            String timestamp = required(headers, "Wechatpay-Timestamp");
            String nonce = required(headers, "Wechatpay-Nonce");
            String signature = required(headers, "Wechatpay-Signature");
            String serial = required(headers, "Wechatpay-Serial");
            JSONObject data = wechatPay.decryptNotify(timestamp, nonce, signature, serial, body);
            if (!"SUCCESS".equals(data.getString("trade_state"))) return ok();
            if (!wechatPay.appId().equals(data.getString("appid")) || !wechatPay.merchantId().equals(data.getString("mchid"))) throw new IllegalArgumentException("商户或小程序不匹配");
            JSONObject amount = data.getJSONObject("amount");
            if (amount == null || !"CNY".equals(amount.getString("currency"))) throw new IllegalArgumentException("支付币种无效");
            String orderNo = data.getString("out_trade_no");
            int paidFen = amount.getIntValue("total");
            String transactionId = data.getString("transaction_id");
            if (orderNo != null && orderNo.startsWith("RB")) {
                var recharge = bossBalancePayments.callbackOrder(orderNo);
                int expectedFen = recharge.getAmount().movePointRight(2).intValueExact();
                if (paidFen != expectedFen) throw new IllegalArgumentException("支付金额与老板钱包充值单不一致");
                bossBalancePayments.markPaid(orderNo, transactionId);
            } else if (orderNo != null && (orderNo.startsWith("BR") || orderNo.startsWith("RR"))) {
                var recharge = rewardRecharges.detailForCallback(orderNo);
                int expectedFen = recharge.getAmount().movePointRight(2).intValueExact();
                if (paidFen != expectedFen) throw new IllegalArgumentException("支付金额与奖励金充值单不一致");
                rewardRecharges.markPaidByWechatCallback(orderNo, transactionId);
            } else if (orderNo != null && orderNo.startsWith("SP")) {
                var payment = settlements.findWechatPayment(orderNo);
                if (paidFen != settlements.expectedWechatPaymentFen(payment)) throw new IllegalArgumentException("支付金额与结算单不一致");
                settlements.markWechatPaymentPaid(orderNo, transactionId);
            } else {
                var order = pointsOrder(orderNo);
                int expectedFen = order.getAmount().movePointRight(2).intValueExact();
                if (paidFen != expectedFen) throw new IllegalArgumentException("支付金额与订单不一致");
                points.markPaidByWechatCallback(orderNo, transactionId);
            }
            return ok();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("code", "FAIL", "message", ex.getMessage() == null ? "回调处理失败" : ex.getMessage()));
        }
    }
    private com.kuaima.app.admin.entity.PointPurchaseOrder pointsOrder(String orderNo) { return orders.findByOrderNo(orderNo).orElseThrow(() -> new IllegalArgumentException("订单不存在")); }
    private String required(HttpHeaders headers, String name) { String value = headers.getFirst(name); if (value == null || value.isBlank()) throw new IllegalArgumentException("缺少回调请求头: " + name); return value; }
    private ResponseEntity<Map<String, String>> ok() { return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功")); }
}
