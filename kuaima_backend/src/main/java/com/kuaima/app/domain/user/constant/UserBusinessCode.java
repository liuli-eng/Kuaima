package com.kuaima.app.domain.user.constant;

import java.security.SecureRandom;

import com.kuaima.app.domain.user.entity.User;

/** 零工/老板对外业务编号，不暴露数据库主键。 */
public final class UserBusinessCode {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BOUND = 10_000_000;

    private UserBusinessCode() {
    }

    public static void ensureWorker(User user) {
        if (user != null && !hasText(user.getWorkerCode())) {
            user.setWorkerCode(generate("G"));
        }
    }

    public static void ensureBoss(User user) {
        if (user != null && !hasText(user.getBossCode())) {
            user.setBossCode(generate("B"));
        }
    }

    private static String generate(String prefix) {
        return prefix + String.format("%07d", RANDOM.nextInt(1, BOUND));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
