package com.vecoo.extraquests.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestFeatureData extends SavedData {
    private final CompoundTag data;

    public QuestFeatureData() {
        this(new CompoundTag());
    }

    private QuestFeatureData(@NotNull CompoundTag data) {
        this.data = data;
    }

    @NotNull
    public static QuestFeatureData get(@NotNull MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(QuestFeatureData::new,
                (tag, provider) -> new QuestFeatureData(tag.getCompound("data")), null), "extraquests_features");
    }

    @NotNull
    private CompoundTag child(@NotNull CompoundTag parent, @NotNull String key) {
        if (!parent.contains(key, CompoundTag.TAG_COMPOUND)) {
            parent.put(key, new CompoundTag());
            setDirty();
        }

        return parent.getCompound(key);
    }

    @NotNull
    public CompoundTag getTeamTask(@NotNull UUID team, long task) {
        return child(child(child(this.data, "teams"), team.toString()), Long.toUnsignedString(task, 16));
    }

    @NotNull
    public CompoundTag getTeamQuest(@NotNull UUID team, long quest) {
        return child(child(child(this.data, "quests"), team.toString()), Long.toUnsignedString(quest, 16));
    }

    @NotNull
    public CompoundTag getQuestSnapshot(@NotNull UUID team) {
        return child(child(this.data, "quests"), team.toString()).copy();
    }

    @NotNull
    public CompoundTag getGlobalTask(long task) {
        return child(child(this.data, "global"), Long.toUnsignedString(task, 16));
    }

    @NotNull
    public CompoundTag getCalendarStreak(@NotNull UUID team, @NotNull String pool) {
        return child(child(child(this.data, "streaks"), team.toString()), pool);
    }

    @NotNull
    public CompoundTag getPlayer(@NotNull UUID player) {
        return child(child(this.data, "players"), player.toString());
    }

    @NotNull
    public CompoundTag getTeamSnapshot(@NotNull UUID team) {
        return child(child(this.data, "teams"), team.toString()).copy();
    }

    public void clearTeamTask(@NotNull UUID team, long task) {
        child(child(this.data, "teams"), team.toString()).remove(Long.toUnsignedString(task, 16));
        setDirty();
    }

    public void clearGlobalTask(long task) {
        child(this.data, "global").remove(Long.toUnsignedString(task, 16));
        setDirty();
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        tag.put("data", this.data.copy());

        return tag;
    }
}
