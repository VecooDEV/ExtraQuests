package com.vecoo.extraquests.runtime;

import com.vecoo.extraquests.data.QuestFeatureData;
import com.vecoo.extraquests.network.QuestClientState;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.task.TaskSettings;
import com.vecoo.extraquests.task.TaskSettingsHolder;
import com.vecoo.extraquests.util.QuestMath;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.StatTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import lombok.val;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TaskProgress {
    private static final ThreadLocal<Set<Long>> WRITING = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Integer> RESETTING = ThreadLocal.withInitial(() -> 0);

    public static boolean internal(@NotNull Task task) {
        return RESETTING.get() > 0 || WRITING.get().contains(task.id);
    }

    @Nullable
    private static MinecraftServer server(@NotNull TeamData teamData) {
        val file = ServerQuestFile.INSTANCE;
        val server = file == null ? null : file.server;

        return server != null && teamData.getFile() != null && teamData.getFile().isServerSide() ? server : null;
    }

    @NotNull
    private static QuestFeatureData savedData(@NotNull TeamData data) {
        val server = server(data);

        if (server == null) {
            throw new IllegalStateException("Quest task state requires an active server.");
        }

        return QuestFeatureData.get(server);
    }

    public static void localReset(@NotNull Runnable work) {
        RESETTING.set(RESETTING.get() + 1);

        try {
            work.run();
        } finally {
            RESETTING.set(RESETTING.get() - 1);
        }
    }

    private static void write(@NotNull Task task, @NotNull Runnable work) {
        val added = WRITING.get().add(task.id);

        try {
            work.run();
        } finally {
            if (added) {
                WRITING.get().remove(task.id);
            }
        }
    }

    private static long rawProgress(@NotNull TeamData data, @NotNull Task task) {
        long[] progress = {0L};

        write(task, () -> progress[0] = data.getProgress(task));

        return progress[0];
    }

    @NotNull
    public static CompoundTag state(@NotNull Task task, @NotNull TeamData teamData) {
        val saved = savedData(teamData);

        if (!TaskSettings.get(task).isShared()) {
            return saved.getTeamTask(teamData.getTeamId(), task.id);
        }

        val state = saved.getGlobalTask(task.id);

        if (!state.contains("progress")) {
            long progress = rawProgress(teamData, task);

            for (TeamData team : teamData.getFile().getAllTeamData()) {
                progress = Math.max(progress, rawProgress(team, task));
            }

            state.putLong("progress", Math.max(0L, Math.min(task.getMaxProgress(), progress)));
            saved.setDirty();
        }

        return state;
    }

    @Nullable
    public static Long sharedProgress(@NotNull TeamData data, long taskId) {
        val server = server(data);

        if (server == null || !server.isSameThread()) {
            return null;
        }

        val task = data.getFile().getTask(taskId);

        if (task == null || internal(task)) {
            return null;
        }

        checkTimeout(task, data, System.currentTimeMillis());

        return TaskSettings.get(task).isShared() ? Math.max(0L, Math.min(task.getMaxProgress(), state(task, data).getLong("progress"))) : null;
    }

    private static boolean available(@NotNull Task task, @NotNull TeamData data) {
        return ((TaskSettingsHolder) task).extraQuests$canAdvance(data);
    }

    @Nullable
    private static ServerPlayer actor(@NotNull TeamData data) {
        val file = ServerQuestFile.INSTANCE;
        val player = file.getCurrentPlayer();

        if (player == null) {
            return null;
        }

        val team = file.getOrCreateTeamData(player);

        return team != null && team.getTeamId().equals(data.getTeamId()) ? player : null;
    }

    @NotNull
    private static String sampleKey(@NotNull TeamData data, @Nullable ServerPlayer player) {
        return player == null ? "team/" + data.getTeamId() : player.getUUID().toString();
    }

    private static boolean cumulative(@NotNull Task task) {
        return task instanceof KeyValueTask || task.consumesResources() || task instanceof ItemTask item && item.isOnlyFromCrafting();
    }

    private static boolean best(@NotNull Task task) {
        val settings = TaskSettings.get(task);

        return settings.getAggregation() == TaskSettings.Aggregation.BEST || settings.getAggregation() == TaskSettings.Aggregation.AUTO && !cumulative(task) && !(task instanceof StatTask);
    }

    private static void participant(@NotNull CompoundTag compoundTag, @Nullable ServerPlayer player) {
        if (player != null) {
            val participants = compoundTag.getCompound("participants");

            participants.putBoolean(player.getUUID().toString(), true);
            compoundTag.put("participants", participants);
        }
    }

    private static void start(@NotNull Task task, @NotNull TeamData data, @Nullable ServerPlayer player, long now) {
        val settings = TaskSettings.get(task);
        val state = state(task, data);

        if (settings.timed() && !state.contains("deadline")) {
            state.putLong("deadline", QuestMath.deadline(now, settings.getDuration()));
            participant(state, player);
            savedData(data).setDirty();
        }
    }

    private static void credit(@NotNull Task task, @NotNull CompoundTag compoundTag, @Nullable ServerPlayer player, long value, boolean absolute) {
        if (player == null || value <= 0L) {
            return;
        }

        val contributors = compoundTag.getCompound("contributors");
        val id = player.getUUID().toString();
        val old = contributors.getLong(id);

        contributors.putLong(id, absolute ? Math.max(old, value) : QuestMath.addProgress(old, value, Long.MAX_VALUE));
        compoundTag.put("contributors", contributors);
        participant(compoundTag, player);
    }

    private static void nativeProgress(@NotNull Task task, @NotNull TeamData data, long progress, @Nullable ServerPlayer player) {
        val file = ServerQuestFile.INSTANCE;

        if (player == null && file.getCurrentPlayer() != null) {
            return;
        }

        Runnable work = () -> write(task, () -> {
            if (available(task, data)) {
                data.setProgress(task, progress);

                if (progress >= task.getMaxProgress() && !data.isCompleted(task) && data.areDependenciesComplete(task.getQuest())) {
                    data.markTaskCompleted(task);
                }
            }
        });

        if (player != null && file.getCurrentPlayer() != player) {
            file.withPlayerContext(player, work);
        } else {
            work.run();
        }
    }

    private static void changed(@NotNull Task task, @NotNull TeamData data, @NotNull ServerPlayer player, @NotNull CompoundTag compoundTag) {
        savedData(data).setDirty();
        nativeProgress(task, data, compoundTag.getLong("progress"), player);
    }

    private static void increment(@NotNull Task task, @NotNull TeamData data, @Nullable ServerPlayer player, long amount) {
        if (amount <= 0L || player == null) {
            return;
        }

        val state = state(task, data);
        val before = state.getLong("progress");

        if (before >= task.getMaxProgress()) {
            return;
        }

        start(task, data, player, System.currentTimeMillis());

        val samples = state.getCompound("samples");
        val key = sampleKey(data, player);
        val sample = QuestMath.addProgress(samples.getLong(key), amount, task.getMaxProgress());

        samples.putLong(key, sample);
        state.put("samples", samples);

        val best = TaskSettings.get(task).getAggregation() == TaskSettings.Aggregation.BEST;
        val next = best ? Math.max(before, sample) : QuestMath.addProgress(before, amount, task.getMaxProgress());

        state.putLong("progress", next);
        credit(task, state, player, best ? sample : next - before, best);
        changed(task, data, player, state);
    }

    private static void sample(@NotNull Task task, @NotNull TeamData data, @Nullable ServerPlayer player, long value, boolean statistic) {
        if (player == null) {
            return;
        }

        checkTimeout(task, data, System.currentTimeMillis());

        val settings = TaskSettings.get(task);
        val state = state(task, data);
        val key = sampleKey(data, player);
        val last = state.getCompound("last_samples");
        val baselines = state.getCompound("baselines");

        if (statistic && settings.timed() && !baselines.contains(key)) {
            baselines.putLong(key, value);
            state.put("baselines", baselines);
        }

        val sampleChanged = !last.contains(key) || last.getLong(key) != value;

        last.putLong(key, value);
        state.put("last_samples", last);

        val effective = Math.max(0L, value - baselines.getLong(key));

        if (!settings.isShared()) {
            if (effective > 0L) {
                start(task, data, player, System.currentTimeMillis());
            }

            write(task, () -> data.setProgress(task, Math.max(0L, Math.min(task.getMaxProgress(), Math.max(data.getProgress(task), effective)))));

            if (sampleChanged) {
                savedData(data).setDirty();
            }

            return;
        }

        val samples = state.getCompound("samples");
        val previous = samples.getLong(key);

        if (effective <= previous) {
            if (sampleChanged) {
                savedData(data).setDirty();
            }

            return;
        }

        samples.putLong(key, effective);
        state.put("samples", samples);
        start(task, data, player, System.currentTimeMillis());

        val before = state.getLong("progress");
        val next = best(task) ? Math.max(before, Math.max(0L, Math.min(task.getMaxProgress(), effective))) : QuestMath.addProgress(before, effective - previous, task.getMaxProgress());

        state.putLong("progress", next);
        credit(task, state, player, best(task) ? effective : next - before, best(task));
        changed(task, data, player, state);
    }

    public static boolean set(@NotNull TeamData data, @NotNull Task task, long value) {
        val settings = TaskSettings.get(task);

        if (internal(task) || server(data) == null || !settings.isShared() && !settings.timed()) {
            return false;
        }

        if (!available(task, data)) {
            return true;
        }

        val player = actor(data);
        val before = data.getProgress(task);
        val amount = Math.max(0L, value - before);

        checkTimeout(task, data, System.currentTimeMillis());

        if (settings.isShared()) {
            if (cumulative(task)) {
                increment(task, data, player, amount);
            } else if (value > 0L) {
                sample(task, data, player, value, task instanceof StatTask);
            }
        } else {
            val progress = cumulative(task) && value > 0L ? QuestMath.addProgress(data.getProgress(task), amount, task.getMaxProgress()) : Math.max(0L, Math.min(task.getMaxProgress(), value));

            if (progress > 0L) {
                start(task, data, player, System.currentTimeMillis());
            }

            write(task, () -> data.setProgress(task, progress));
        }

        return true;
    }

    public static boolean add(@NotNull TeamData data, @NotNull Task task, long amount) {
        val settings = TaskSettings.get(task);

        if (internal(task) || server(data) == null || !settings.isShared() && !settings.timed()) {
            return false;
        }

        if (!available(task, data) || amount <= 0L) {
            return true;
        }

        checkTimeout(task, data, System.currentTimeMillis());

        val player = actor(data);

        if (settings.isShared()) {
            increment(task, data, player, amount);
        } else if (!data.isCompleted(task)) {
            start(task, data, player, System.currentTimeMillis());
            write(task, () -> data.setProgress(task, QuestMath.addProgress(data.getProgress(task), amount, task.getMaxProgress())));
        }

        return true;
    }

    public static void statSample(@NotNull Task task, @NotNull TeamData data, @NotNull ServerPlayer player, long value) {
        if (server(data) != null && !data.isCompleted(task) && available(task, data)) {
            sample(task, data, player, value, true);
        }
    }

    public static long comparison(@NotNull Task task, @NotNull TeamData data, @NotNull ServerPlayer player) {
        if (server(data) != null && TaskSettings.get(task).isShared()) {
            return state(task, data).getCompound("last_samples").getLong(sampleKey(data, player));
        }

        return data.getProgress(task);
    }

    private static void resetState(@NotNull Task task, @NotNull TeamData data) {
        val saved = savedData(data);
        val previous = state(task, data).copy();
        val shared = TaskSettings.get(task).isShared();

        if (shared) {
            saved.clearGlobalTask(task.id);
        } else {
            saved.clearTeamTask(data.getTeamId(), task.id);
        }

        val next = shared ? saved.getGlobalTask(task.id) : saved.getTeamTask(data.getTeamId(), task.id);

        if (shared) {
            next.putLong("progress", 0L);
        }

        if (previous.contains("calendar_period")) {
            next.putLong("calendar_period", previous.getLong("calendar_period"));
        }

        if (task instanceof StatTask) {
            next.put("baselines", previous.getCompound("last_samples").copy());
            next.put("last_samples", previous.getCompound("last_samples").copy());
        }

        saved.setDirty();
    }

    private static void resetAttempt(@NotNull Task task, @NotNull TeamData data) {
        resetState(task, data);

        val teams = TaskSettings.get(task).isShared() ? new ArrayList<>(data.getFile().getAllTeamData()) : List.of(data);

        for (TeamData team : teams) {
            if (!team.isLocked()) {
                write(task, () -> team.setProgress(task, 0L));
            }
        }
    }

    public static void onReset(@NotNull Task task, @NotNull TeamData data) {
        if (internal(task) || server(data) == null || data.isLocked()) {
            return;
        }

        if (TaskSettings.get(task).isShared()) {
            resetShared(task, data);
        } else {
            resetState(task, data);
        }
    }

    public static void clearLocal(@NotNull Task task, @NotNull TeamData data) {
        if (server(data) != null && !TaskSettings.get(task).isShared()) {
            resetState(task, data);
        }
    }

    public static void resetShared(@NotNull Task task, @NotNull MinecraftServer server) {
        val teams = ServerQuestFile.INSTANCE.getAllTeamData();

        if (teams.isEmpty()) {
            val saved = QuestFeatureData.get(server);

            saved.clearGlobalTask(task.id);
            saved.getGlobalTask(task.id).putLong("progress", 0L);
            saved.setDirty();
        } else {
            resetShared(task, teams.iterator().next());
        }
    }

    public static void resetShared(@NotNull Task task, @NotNull TeamData data) {
        resetState(task, data);

        for (TeamData team : new ArrayList<>(data.getFile().getAllTeamData())) {
            QuestFeatures.resetQuest(task.getQuest(), team);
        }
    }

    public static void prepareCalendar(@NotNull Quest quest, @NotNull TeamData data, long period) {
        val shared = quest.getTasks().stream().filter(task -> TaskSettings.get(task).isShared()).toList();

        if (shared.isEmpty() || shared.stream().allMatch(task -> state(task, data).contains("calendar_period")
                && state(task, data).getLong("calendar_period") == period)) {
            return;
        }

        for (Task task : shared) {
            resetState(task, data);
            state(task, data).putLong("calendar_period", period);
        }

        for (TeamData team : new ArrayList<>(data.getFile().getAllTeamData())) {
            QuestFeatures.resetQuest(quest, team);
        }
    }

    public static void checkTimeout(@NotNull Task task, @NotNull TeamData data, long now) {
        if (!TaskSettings.get(task).timed() || data.isLocked()) {
            return;
        }

        val state = state(task, data);
        val completed = TaskSettings.get(task).isShared() ? state.getLong("progress") >= task.getMaxProgress() : data.isCompleted(task);

        if (!completed && state.contains("deadline") && now >= state.getLong("deadline")) {
            resetAttempt(task, data);
        }
    }

    public static void tick(@NotNull TeamData data, @NotNull List<Task> tasks, long now) {
        if (server(data) == null || data.isLocked()) {
            return;
        }

        val player = data.getOnlineMembers().stream().findFirst().orElse(null);

        for (Task task : tasks) {
            val settings = TaskSettings.get(task);

            if (settings.timed()) {
                checkTimeout(task, data, now);

                if (player != null && settings.getTimerStart() == TaskSettings.TimerStart.AVAILABLE && data.getProgress(task) < task.getMaxProgress() && available(task, data)) {
                    start(task, data, player, now);
                }
            }
        }
    }

    public static void syncShared(@NotNull MinecraftServer server) {
        val file = ServerQuestFile.INSTANCE;

        if (file == null || file.getCurrentPlayer() != null) {
            return;
        }

        val tasks = file.collect(Task.class).stream().filter(task -> TaskSettings.get(task).isShared()).toList();

        for (TeamData team : new ArrayList<>(file.getAllTeamData())) {
            for (Task task : tasks) {
                if (!team.isLocked()) {
                    val progress = state(task, team).getLong("progress");
                    val player = team.getOnlineMembers().stream().filter(member -> task.getQuest().getTasks().stream().allMatch(goal -> canClaim(goal, member.getUUID())))
                            .findFirst().orElse(null);

                    nativeProgress(task, team, progress, player);

                    if (player != null) {
                        file.withPlayerContext(player, () -> team.checkAutoCompletion(task.getQuest()));
                    }
                }
            }
        }
    }

    public static void fail(@NotNull ServerPlayer player, @NotNull String reason) {
        val file = ServerQuestFile.INSTANCE;
        val data = file == null ? null : file.getOrCreateTeamData(player);

        if (data == null || data.isLocked()) {
            return;
        }

        for (Task task : file.collect(Task.class)) {
            val settings = TaskSettings.get(task);

            if (!settings.timed() || !settings.resetsFor(reason)) {
                continue;
            }

            val state = state(task, data);

            if (state.contains("deadline") && data.getProgress(task) < task.getMaxProgress()
                    && (!settings.isShared() || state.getCompound("participants").contains(player.getUUID().toString()))) {
                resetAttempt(task, data);
            }
        }
    }

    public static boolean canClaim(@NotNull Task task, @NotNull UUID playerUUID) {
        val settings = TaskSettings.get(task);

        if (!settings.isShared()) {
            return true;
        }

        val file = ServerQuestFile.INSTANCE;
        val server = task.getQuestFile().isServerSide() && file != null ? file.server : null;
        val state = server == null ? QuestClientState.global(task.id) : QuestFeatureData.get(server).getGlobalTask(task.id);
        val contribution = server == null ? state.getLong("personal") : state.getCompound("contributors").getLong(playerUUID.toString());

        return state.getLong("progress") >= task.getMaxProgress() && contribution >= settings.minimum(task);
    }

    @NotNull
    public static CompoundTag clientState(@NotNull Task task) {
        return TaskSettings.get(task).isShared() ? QuestClientState.global(task.id) : QuestClientState.task(task.id);
    }

    @NotNull
    public static CompoundTag snapshot(@NotNull Task task, @NotNull TeamData data, @NotNull UUID playerUUID) {
        val state = state(task, data).copy();

        state.putLong("personal", state.getCompound("contributors").getLong(playerUUID.toString()));

        for (String key : List.of("contributors", "samples", "last_samples", "baselines", "participants")) {
            state.remove(key);
        }

        return state;
    }

    public static void tooltip(@NotNull Task task, @NotNull TooltipList list, @NotNull TeamData data) {
        val settings = TaskSettings.get(task);
        val state = clientState(task);

        if (settings.isShared()) {
            list.translate("extraquests.options.shared_tooltip");
            if (settings.minimum(task) > 0L) {
                list.translate("extraquests.shared.personal", state.getLong("personal"), settings.minimum(task));
            }
        }

        if (settings.timed()) {
            if (data.isCompleted(task)) {
                list.translate("extraquests.challenge.completed");
            } else if (!state.contains("deadline")) {
                list.translate("extraquests.challenge.not_started");
            } else {
                list.translate("extraquests.challenge.remaining", Math.max(0L, (state.getLong("deadline") - System.currentTimeMillis() + 999L) / 1000L));
            }
        }
    }
}
