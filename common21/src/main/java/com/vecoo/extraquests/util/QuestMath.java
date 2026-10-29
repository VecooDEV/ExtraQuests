package com.vecoo.extraquests.util;

import lombok.val;

public class QuestMath {
    public static long addProgress(long current, long amount, long maximum) {
        val progress = Math.clamp(current, 0L, maximum);

        return amount <= 0L ? progress : progress + Math.min(amount, maximum - progress);
    }

    public static long deadline(long now, long seconds) {
        return now + Math.clamp(seconds, 0L, (Long.MAX_VALUE - now) / 1000L) * 1000L;
    }
}
