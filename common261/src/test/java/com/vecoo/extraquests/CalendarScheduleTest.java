package com.vecoo.extraquests;

import com.vecoo.extraquests.util.CalendarSchedule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarScheduleTest {
    private long time(String instant) {
        return Instant.parse(instant).toEpochMilli();
    }

    private long period(String instant, String zone, boolean weekly) {
        return CalendarSchedule.period(time(instant), ZoneId.of(zone), LocalTime.of(6, 0), weekly, DayOfWeek.MONDAY);
    }

    @Test
    void dailyResetHasOneBoundaryAtConfiguredLocalTime() {
        long previous = period("2026-10-28T05:59:59Z", "UTC", false);
        assertEquals(previous + 1L, period("2026-10-28T06:00:00Z", "UTC", false));
        assertEquals(previous, period("2026-10-28T00:00:00Z", "UTC", false));
    }

    @Test
    void weeklyResetWaitsForMondayResetTime() {
        long sunday = period("2026-10-25T23:00:00Z", "UTC", true);
        assertEquals(sunday, period("2026-10-26T05:59:59Z", "UTC", true));
        assertEquals(sunday + 7L, period("2026-10-26T06:00:00Z", "UTC", true));
    }

    @Test
    void daylightSavingUsesCalendarDaysRatherThan24Hours() {
        ZoneId zone = ZoneId.of("Europe/Berlin");
        LocalTime reset = LocalTime.of(6, 0);
        long spring = CalendarSchedule.period(time("2026-03-28T06:00:00Z"), zone, reset, false, DayOfWeek.MONDAY);
        assertEquals(time("2026-03-29T04:00:00Z"), CalendarSchedule.nextReset(spring, zone, reset, false));
        long autumn = CalendarSchedule.period(time("2026-10-24T06:00:00Z"), zone, reset, false, DayOfWeek.MONDAY);
        assertEquals(time("2026-10-25T05:00:00Z"), CalendarSchedule.nextReset(autumn, zone, reset, false));
    }

    @Test
    void poolSelectionSurvivesRestartAndRegistryOrderChanges() {
        UUID team = UUID.fromString("00000000-0000-0000-0000-000000000123");
        List<Long> ids = new ArrayList<>(List.of(1L, 2L, 3L, 4L, 5L, -1L));
        List<Long> selected = CalendarSchedule.select(ids, team, "daily", 20000L, 3);
        Collections.reverse(ids);
        assertEquals(selected, CalendarSchedule.select(ids, team, "daily", 20000L, 3));
        assertEquals(3, selected.stream().distinct().count());
        assertEquals(6, CalendarSchedule.select(ids, team, "daily", 20000L, 100).size());
        assertTrue(CalendarSchedule.select(List.of(), team, "empty", 20000L, 3).isEmpty());
    }
}
