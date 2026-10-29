package com.vecoo.extraquests.runtime;

import com.vecoo.extraquests.ExtraQuestsConstants;
import com.vecoo.extraquests.data.QuestFeatureData;
import com.vecoo.extraquests.network.QuestClientState;
import com.vecoo.extraquests.network.QuestStatePayload;
import com.vecoo.extraquests.quest.QuestSettings;
import com.vecoo.extraquests.reward.AttributeReward;
import com.vecoo.extraquests.reward.CalendarStreakReward;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.task.PlayerNbtTask;
import com.vecoo.extraquests.task.TaskSettings;
import com.vecoo.extraquests.util.CalendarSchedule;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.SyncTeamDataMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import lombok.val;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class QuestFeatures {
    private static BiConsumer<ServerPlayer, QuestStatePayload> stateSender;
    private static final Map<UUID, CompoundTag> LAST_SNAPSHOTS = new HashMap<>();

    public static void registerBase() {
        KeyValueTask.TYPE = TaskTypes.register(id("key_value"), KeyValueTask::new, () -> Icon.getIcon("minecraft:item/paper"));
        KeyValueReward.TYPE = RewardTypes.register(id("key_value"), KeyValueReward::new, () -> Icon.getIcon("minecraft:item/paper"));
    }

    public static void register(@NotNull BiConsumer<ServerPlayer, QuestStatePayload> sender) {
        stateSender = Objects.requireNonNull(sender);

        PlayerNbtTask.TYPE = TaskTypes.register(id("player_nbt"), PlayerNbtTask::new, () -> Icon.getIcon("minecraft:item/name_tag"));
        AttributeReward.TYPE = RewardTypes.register(id("attribute"), AttributeReward::new, () -> Icon.getIcon("minecraft:item/golden_apple"));
        CalendarStreakReward.TYPE = RewardTypes.register(id("calendar_streak"), CalendarStreakReward::new, () -> Icon.getIcon("minecraft:item/diamond"));
    }

    @NotNull
    private static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(ExtraQuestsConstants.MOD_ID, path);
    }

    public static boolean canStart(@NotNull TeamData data, @NotNull Quest quest) {
        val calendar = QuestSettings.get(quest);

        if (!calendar.isCalendarEnabled()) {
            return true;
        }

        val file = ServerQuestFile.INSTANCE;
        val server = file == null ? null : file.server;

        if (server != null && data.getFile().isServerSide()) {
            val state = QuestFeatureData.get(server).getTeamQuest(data.getTeamId(), quest.id);

            return state.getBoolean("active") && state.contains("period") && state.getLong("period") == calendar.period(System.currentTimeMillis());
        }

        return QuestClientState.quest(quest.id).getBoolean("active");
    }

    public static boolean canClaim(@NotNull UUID player, @NotNull Reward reward) {
        return reward.getQuest().getTasks().stream().allMatch(task -> TaskProgress.canClaim(task, player));
    }

    public static boolean usesScheduledReset(@NotNull Quest quest) {
        return QuestSettings.get(quest).isCalendarEnabled() || quest.getTasks().stream().anyMatch(task -> TaskSettings.get(task).isShared());
    }

    public static void onCompleted(@NotNull Quest quest, @NotNull TeamData data) {
        val file = ServerQuestFile.INSTANCE;
        val server = file == null ? null : file.server;

        if (server != null && data.getFile().isServerSide() && QuestSettings.get(quest).isCalendarEnabled()) {
            QuestSettings.get(quest).onCompleted(quest, data, server);
        }
    }

    public static void resetQuest(@NotNull Quest quest, @Nullable TeamData data) {
        if (data == null || data.isLocked()) {
            return;
        }

        for (Task task : quest.getTasks()) {
            if (!TaskSettings.get(task).isShared()) {
                TaskProgress.clearLocal(task, data);
            }
        }

        TaskProgress.localReset(() -> quest.forceProgressRaw(data, new ProgressChange(quest, Util.NIL_UUID)));
        quest.getRewards().forEach(data::deleteReward);
        data.clearRepeatCooldown(quest);

        for (ServerPlayer player : data.getOnlineMembers()) {
            NetworkHelper.sendTo(player, new SyncTeamDataMessage(data));
        }
    }

    public static void prepareCalendars(@NotNull MinecraftServer server, @Nullable TeamData data, @NotNull List<Quest> calendars, long now) {
        if (data == null || data.isLocked()) {
            return;
        }

        val saved = QuestFeatureData.get(server);
        val scheduled = calendars.stream().filter(quest -> QuestSettings.get(quest).isCalendarEnabled()).toList();
        val pools = scheduled.stream().filter(quest -> !QuestSettings.get(quest).getPoolKey().isEmpty())
                .collect(Collectors.groupingBy(quest -> QuestSettings.get(quest).getPoolKey()));
        Map<String, Set<Long>> selected = new HashMap<>();

        pools.forEach((pool, quests) -> selected.put(pool, new HashSet<>(CalendarSchedule.select(
                quests.stream().map(quest -> quest.id).toList(), data.getTeamId(), pool, QuestSettings.get(quests.getFirst()).period(now),
                quests.stream().mapToInt(quest -> QuestSettings.get(quest).getActiveQuests()).min().orElse(1)))));

        for (Quest quest : scheduled) {
            val calendar = QuestSettings.get(quest);
            val state = saved.getTeamQuest(data.getTeamId(), quest.id);
            val period = calendar.period(now);

            if (!state.contains("period") || state.getLong("period") != period) {
                TaskProgress.prepareCalendar(quest, data, period);
                resetQuest(quest, data);
                state.putLong("period", period);
                saved.setDirty();
            }

            val nextReset = calendar.nextReset(period);

            if (state.getLong("next_reset") != nextReset) {
                state.putLong("next_reset", nextReset);
                saved.setDirty();
            }

            val streak = calendar.getStreak(quest, data, server);

            if (state.getInt("streak") != streak) {
                state.putInt("streak", streak);
                saved.setDirty();
            }

            val active = calendar.getPoolKey().isEmpty() || selected.get(calendar.getPoolKey()).contains(quest.id);

            if (state.getBoolean("active") != active) {
                state.putBoolean("active", active);
                saved.setDirty();
            }
        }
    }

    public static void failChallenges(@NotNull ServerPlayer player, @NotNull String reason) {
        TaskProgress.fail(player, reason);
    }

    public static void logout(@NotNull ServerPlayer player) {
        failChallenges(player, "logout");
        LAST_SNAPSHOTS.remove(player.getUUID());
    }

    public static void tick(@NotNull MinecraftServer server) {
        val file = ServerQuestFile.INSTANCE;

        if (file == null || server.getTickCount() % 20 != 0) {
            return;
        }

        val calendars = file.collect(Quest.class).stream().filter(quest -> QuestSettings.get(quest).isCalendarEnabled()).toList();
        val allTasks = file.collect(Task.class);

        for (TeamData data : new ArrayList<>(file.getAllTeamData())) {
            prepareCalendars(server, data, calendars, System.currentTimeMillis());
            TaskProgress.tick(data, allTasks, System.currentTimeMillis());
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            val data = file.getOrCreateTeamData(player);

            AttributeBonuses.refresh(player, System.currentTimeMillis());
            if (data == null) {
                continue;
            }

            sync(player, data);
        }

        TaskProgress.syncShared(server);
    }

    public static void sync(@NotNull ServerPlayer player, @NotNull TeamData data) {
        val saved = QuestFeatureData.get(player.server);
        val snapshot = new CompoundTag();

        snapshot.put("tasks", saved.getTeamSnapshot(data.getTeamId()));
        snapshot.put("quests", saved.getQuestSnapshot(data.getTeamId()));

        val global = new CompoundTag();

        for (Task task : ServerQuestFile.INSTANCE.collect(Task.class)) {
            if (TaskSettings.get(task).isShared()) {
                global.put(Long.toUnsignedString(task.id, 16), TaskProgress.snapshot(task, data, player.getUUID()));
            }
        }

        snapshot.put("global", global);

        if (!snapshot.equals(LAST_SNAPSHOTS.get(player.getUUID()))) {
            stateSender.accept(player, new QuestStatePayload(snapshot));
            LAST_SNAPSHOTS.put(player.getUUID(), snapshot);
        }
    }

    public static void stopped() {
        LAST_SNAPSHOTS.clear();
    }
}
