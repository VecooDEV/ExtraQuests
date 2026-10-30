package com.vecoo.extraquests.quest;

import com.vecoo.extraquests.data.QuestFeatureData;
import com.vecoo.extraquests.util.CalendarSchedule;
import com.vecoo.extraquests.util.FeatureOptions;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

@Getter
@Setter
public class QuestSettings {
    private boolean calendarEnabled = false;
    private boolean weekly = false;
    @NotNull
    private String resetTime = "06:00";
    @NotNull
    private String timezone = "UTC";
    private int weekday = 1;
    @NotNull
    private String pool = "";
    private int activeQuests = 3;

    @NotNull
    public static QuestSettings get(@NotNull Quest quest) {
        return ((QuestSettingsHolder) (Object) quest).extraQuests$getSettings();
    }

    public long period(long now) {
        return CalendarSchedule.period(now, ZoneId.of(this.timezone), LocalTime.parse(this.resetTime), this.weekly, DayOfWeek.of(this.weekday));
    }

    public long nextReset(long period) {
        return CalendarSchedule.nextReset(period, ZoneId.of(this.timezone), LocalTime.parse(this.resetTime), this.weekly);
    }

    public long previousPeriod(long period) {
        return period - (this.weekly ? 7L : 1L);
    }

    @NotNull
    public String getPoolKey() {
        return this.pool.isBlank() ? "" : this.pool + "/" + this.weekly + "/" + this.resetTime + "/" + this.timezone + (this.weekly ? "/" + this.weekday : "");
    }

    @NotNull
    public String getStreakKey(@NotNull Quest quest) {
        return getPoolKey().isEmpty() ? "quest/" + Long.toUnsignedString(quest.id, 16) : "pool/" + getPoolKey();
    }

    public int getStreak(@NotNull Quest quest, @NotNull TeamData data, @NotNull MinecraftServer server) {
        val state = QuestFeatureData.get(server).getCalendarStreak(data.getTeamId(), getStreakKey(quest));
        val current = period(System.currentTimeMillis());

        return state.contains("completed_period") && state.getLongOr("completed_period", 0L) >= previousPeriod(current)
                && state.getLongOr("completed_period", 0L) <= current ? state.getIntOr("streak", 0) : 0;
    }

    public void onCompleted(@NotNull Quest quest, @NotNull TeamData data, @NotNull MinecraftServer server) {
        val saved = QuestFeatureData.get(server);
        val state = saved.getCalendarStreak(data.getTeamId(), getStreakKey(quest));
        val current = period(System.currentTimeMillis());

        if (!state.contains("completed_period") || state.getLongOr("completed_period", 0L) != current) {
            val streak = state.contains("completed_period") && state.getLongOr("completed_period", 0L) == previousPeriod(current)
                    ? Math.min(Integer.MAX_VALUE - 1, state.getIntOr("streak", 0)) + 1 : 1;

            state.putInt("streak", streak);
            state.putLong("completed_period", current);
            saved.setDirty();
        }
    }

    @NotNull
    public CompoundTag write() {
        val tag = new CompoundTag();

        tag.putBoolean("enabled", this.calendarEnabled);
        tag.putBoolean("weekly", this.weekly);
        tag.putString("reset_time", this.resetTime);
        tag.putString("timezone", this.timezone);
        tag.putInt("weekday", this.weekday);
        tag.putString("pool", this.pool);
        tag.putInt("active_quests", this.activeQuests);

        return tag;
    }

    public void read(@NotNull CompoundTag tag) {
        this.calendarEnabled = tag.getBooleanOr("enabled", false);
        this.weekly = tag.getBooleanOr("weekly", false);
        this.resetTime = validTime(tag.getStringOr("reset_time", ""));
        this.timezone = validZone(tag.getStringOr("timezone", ""));
        this.weekday = tag.contains("weekday") ? Math.clamp(tag.getIntOr("weekday", 0), 1, 7) : 1;
        this.pool = tag.getStringOr("pool", "");
        this.activeQuests = tag.contains("active_quests") ? Math.clamp(tag.getIntOr("active_quests", 0), 1, 1000) : 3;
    }

    @NotNull
    private static String validTime(@NotNull String value) {
        try {
            return LocalTime.parse(value).toString();
        } catch (DateTimeException e) {
            return "06:00";
        }
    }

    @NotNull
    private static String validZone(@NotNull String value) {
        try {
            return ZoneId.of(value).getId();
        } catch (DateTimeException e) {
            return "UTC";
        }
    }

    public void fillConfigGroup(@NotNull EditableConfigGroup parent) {
        val calendar = parent.getOrCreateSubgroup("extraquests_calendar").setNameKey("extraquests.calendar.settings");
        val enabled = calendar.addBool("enabled", this.calendarEnabled, this::setCalendarEnabled, false);

        enabled.setNameKey("extraquests.calendar.enabled").setOrder(-1);
        val weekly = calendar.addBool("weekly", this.weekly, this::setWeekly, false);

        weekly.setNameKey("extraquests.calendar.weekly").setCanEdit(enabled::getValue);
        calendar.addString("reset_time", this.resetTime, value -> this.resetTime = validTime(value), "06:00")
                .setNameKey("extraquests.calendar.reset_time").setCanEdit(enabled::getValue);
        calendar.addString("timezone", this.timezone, value -> this.timezone = validZone(value), "UTC")
                .setNameKey("extraquests.calendar.timezone").setCanEdit(enabled::getValue);
        calendar.addEnum("weekday", this.weekday, this::setWeekday, FeatureOptions.DAYS)
                .setNameKey("extraquests.calendar.weekday").setCanEdit(() -> enabled.getValue() && weekly.getValue());

        val pool = calendar.addString("pool", this.pool, this::setPool, "");

        pool.setNameKey("extraquests.calendar.pool").setCanEdit(enabled::getValue);
        calendar.addInt("active_quests", this.activeQuests, this::setActiveQuests, 3, 1, 1000)
                .setNameKey("extraquests.calendar.active_quests").setCanEdit(() -> enabled.getValue() && !pool.getValue().isBlank());
    }
}
