package com.kuaima.app.domain.reward.service;

/** 微信商家转账发起失败；失败单和退回流水已在抛出前独立提交。 */
public class WorkerRewardWithdrawalFailedException extends RuntimeException {
    public WorkerRewardWithdrawalFailedException(String message) {
        super(message);
    }
}
