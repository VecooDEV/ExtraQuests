package com.vecoo.extraquests.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestFeatureData extends SavedData {
    private static final SavedDataType<QuestFeatureData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("extraquests", "features"),
            QuestFeatureData::new, CompoundTag.CODEC.fieldOf("data").xmap(QuestFeatureData::new, saved -> saved.data).codec(), null);

    private final CompoundTag data;

    public QuestFeatureData() {
        this(new CompoundTag());
    }

    private QuestFeatureData(@NotNull CompoundTag data) {
        this.data = data;
    }

    @NotNull
    public static QuestFeatureData get(@NotNull MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    @NotNull
    private CompoundTag child(@NotNull CompoundTag parent, @NotNull String key) {
        if (!(parent.get(key) instanceof CompoundTag)) {
            parent.put(key, new CompoundTag());
            setDirty();
        }

        return parent.getCompoundOrEmpty(key);
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

}
