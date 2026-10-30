package com.vecoo.extraquests.util;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CalendarSchedule {
    public static long period(long now, @NotNull ZoneId zone, @NotNull LocalTime reset, boolean weekly, @NotNull DayOfWeek day) {
        val instant = Instant.ofEpochMilli(now);
        LocalDate date = instant.atZone(zone).toLocalDate();

        if (instant.isBefore(date.atTime(reset).atZone(zone).toInstant())) {
            date = date.minusDays(1L);
        }

        return (weekly ? date.with(TemporalAdjusters.previousOrSame(day)) : date).toEpochDay();
    }

    public static long nextReset(long period, @NotNull ZoneId zone, @NotNull LocalTime reset, boolean weekly) {
        return LocalDate.ofEpochDay(period).plusDays(weekly ? 7L : 1L).atTime(reset).atZone(zone).toInstant().toEpochMilli();
    }

    @NotNull
    public static List<Long> select(@NotNull List<Long> ids, @NotNull UUID team, @NotNull String pool, long period, int amount) {
        if (ids.isEmpty()) {
            return List.of();
        }

        val selected = new ArrayList<>(ids);

        selected.sort(Long::compareUnsigned);

        val seed = team.getMostSignificantBits() ^ Long.rotateLeft(team.getLeastSignificantBits(), 17)
                ^ Long.rotateLeft(period, 31) ^ pool.hashCode();

        Collections.shuffle(selected, new Random(seed));

        return List.copyOf(selected.subList(0, Math.max(1, Math.min(selected.size(), amount))));
    }
}
