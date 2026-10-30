package com.vecoo.extraquests.task;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.quest.task.Task;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Getter
@Setter
public class TaskSettings {
    public enum Aggregation {
        AUTO, SUM, BEST;

        public static final NameMap<Aggregation> NAME_MAP = NameMap.of(AUTO, values())
                .id(value -> value.name().toLowerCase(Locale.ROOT)).baseNameKey("extraquests.aggregation").create();
    }

    public enum TimerStart {
        FIRST_PROGRESS, AVAILABLE;

        public static final NameMap<TimerStart> NAME_MAP = NameMap.of(FIRST_PROGRESS, values())
                .id(value -> value.name().toLowerCase(Locale.ROOT)).baseNameKey("extraquests.timer_start").create();
    }

    private boolean shared = false;
    @NotNull
    private Aggregation aggregation = Aggregation.AUTO;
    private long minimumContribution = 0L;
    private boolean timerEnabled = false;
    private long duration = 600L;
    @NotNull
    private TimerStart timerStart = TimerStart.FIRST_PROGRESS;
    private boolean resetOnDeath = true;
    private boolean resetOnDamage = false;
    private boolean resetOnLogout = false;

    @NotNull
    public static TaskSettings get(@NotNull Task task) {
        return ((TaskSettingsHolder) task).extraQuests$getSettings();
    }

    public boolean timed() {
        return this.timerEnabled && this.duration > 0L;
    }

    public long minimum(@NotNull Task task) {
        return Math.clamp(this.minimumContribution, 0L, task.getMaxProgress());
    }

    public boolean resetsFor(@NotNull String reason) {
        return switch (reason) {
            case "death" -> this.resetOnDeath;
            case "damage" -> this.resetOnDamage;
            case "logout" -> this.resetOnLogout;
            default -> false;
        };
    }

    @NotNull
    public CompoundTag write() {
        val tag = new CompoundTag();

        tag.putBoolean("shared", this.shared);
        tag.putString("aggregation", this.aggregation.name());
        tag.putLong("minimum_contribution", this.minimumContribution);
        tag.putBoolean("timer_enabled", this.timerEnabled);
        tag.putLong("duration", this.duration);
        tag.putString("timer_start", this.timerStart.name());
        tag.putBoolean("reset_death", this.resetOnDeath);
        tag.putBoolean("reset_damage", this.resetOnDamage);
        tag.putBoolean("reset_logout", this.resetOnLogout);

        return tag;
    }

    public void read(@NotNull CompoundTag tag) {
        this.shared = tag.getBooleanOr("shared", false);
        this.aggregation = enumValue(Aggregation.class, tag.getStringOr("aggregation", ""), Aggregation.AUTO);
        this.minimumContribution = Math.max(0L, tag.getLongOr("minimum_contribution", 0L));
        this.timerEnabled = tag.getBooleanOr("timer_enabled", false);
        this.duration = tag.contains("duration") ? Math.clamp(tag.getLongOr("duration", 0L), 1L, 31536000L) : 600L;
        this.timerStart = enumValue(TimerStart.class, tag.getStringOr("timer_start", ""), TimerStart.FIRST_PROGRESS);
        this.resetOnDeath = !tag.contains("reset_death") || tag.getBooleanOr("reset_death", false);
        this.resetOnDamage = tag.getBooleanOr("reset_damage", false);
        this.resetOnLogout = tag.getBooleanOr("reset_logout", false);
    }

    @NotNull
    private static <E extends Enum<E>> E enumValue(@NotNull Class<E> type, @NotNull String value, @NotNull E fallback) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public void fillConfigGroup(@NotNull EditableConfigGroup parent, @NotNull Task task) {
        val progress = parent.getOrCreateSubgroup("extraquests_progress").setNameKey("extraquests.options.progress");
        val shared = progress.addBool("shared", this.shared, this::setShared, false);

        shared.setNameKey("extraquests.options.shared").setOrder(-1);
        progress.addEnum("aggregation", this.aggregation, this::setAggregation, Aggregation.NAME_MAP)
                .setNameKey("extraquests.options.aggregation").setCanEdit(shared::getValue);
        progress.addLong("minimum_contribution", this.minimumContribution, this::setMinimumContribution, 0L, 0L,
                Math.max(1L, task.getMaxProgress())).setNameKey("extraquests.options.minimum").setCanEdit(shared::getValue);

        val timer = parent.getOrCreateSubgroup("extraquests_timer").setNameKey("extraquests.options.timer");
        val enabled = timer.addBool("enabled", this.timerEnabled, this::setTimerEnabled, false);

        enabled.setNameKey("extraquests.options.timer_enabled").setOrder(-1);
        timer.addLong("duration", this.duration, this::setDuration, 600L, 1L, 31536000L)
                .setNameKey("extraquests.options.duration").setCanEdit(enabled::getValue);
        timer.addEnum("timer_start", this.timerStart, this::setTimerStart, TimerStart.NAME_MAP)
                .setNameKey("extraquests.options.timer_start").setCanEdit(enabled::getValue);
        timer.addBool("reset_death", this.resetOnDeath, this::setResetOnDeath, true)
                .setNameKey("extraquests.challenge.reset_death").setCanEdit(enabled::getValue);
        timer.addBool("reset_damage", this.resetOnDamage, this::setResetOnDamage, false)
                .setNameKey("extraquests.challenge.reset_damage").setCanEdit(enabled::getValue);
        timer.addBool("reset_logout", this.resetOnLogout, this::setResetOnLogout, false)
                .setNameKey("extraquests.challenge.reset_logout").setCanEdit(enabled::getValue);
    }
}
