package com.vecoo.extraquests;

import com.vecoo.extraquests.util.QuestMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestMathTest {
    @Test
    void incrementsSaturateBeforeOverflow() {
        assertEquals(100L, QuestMath.addProgress(13L, Long.MAX_VALUE, 100L));
        assertEquals(Long.MAX_VALUE, QuestMath.addProgress(1L, Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(0L, QuestMath.addProgress(-1L, 0L, 100L));
        assertEquals(13L, QuestMath.addProgress(13L, -1L, 100L));
    }

    @Test
    void deadlineHandlesLargeDurationWithoutOverflow() {
        assertEquals(60001L, QuestMath.deadline(1L, 60L));
        assertEquals(Long.MAX_VALUE, QuestMath.deadline(Long.MAX_VALUE - 1000L, Long.MAX_VALUE));
    }
}
