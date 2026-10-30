package com.vecoo.extraquests.util;

import lombok.val;

public class QuestMath {
    public static long addProgress(long current, long amount, long maximum) {
        val progress = Math.max(0L, Math.min(maximum, current));

        return amount <= 0L ? progress : progress + Math.min(amount, maximum - progress);
    }

    public static long deadline(long now, long seconds) {
        return now + Math.max(0L, Math.min((Long.MAX_VALUE - now) / 1000L, seconds)) * 1000L;
    }
}
