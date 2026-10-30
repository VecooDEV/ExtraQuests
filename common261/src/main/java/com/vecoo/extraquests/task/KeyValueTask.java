package com.vecoo.extraquests.task;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ISingleLongValueTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

@Getter
public class KeyValueTask extends Task implements ISingleLongValueTask {
    public static TaskType TYPE;

    @NotNull
    private String key;
    @Setter
    private long value;

    public KeyValueTask(long id, @NotNull Quest quest) {
        super(id, quest);
        this.key = "key";
        this.value = 100L;
    }

    @Override
    @NotNull
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public long getMaxProgress() {
        return this.value;
    }

    @Override
    public void writeData(@NotNull Json5Object compoundTag, @NotNull HolderLookup.Provider provider) {
        super.writeData(compoundTag, provider);
        compoundTag.addProperty("key", this.key);
        compoundTag.addProperty("value", this.value);
    }

    @Override
    public void readData(@NotNull Json5Object compoundTag, @NotNull HolderLookup.Provider provider) {
        super.readData(compoundTag, provider);
        this.key = Json5Util.getString(compoundTag, "key").orElse("");
        this.value = Json5Util.getLong(compoundTag, "value").orElse(0L);
    }

    @Override
    public void writeNetData(@NotNull RegistryFriendlyByteBuf byteBuf) {
        super.writeNetData(byteBuf);
        byteBuf.writeUtf(this.key, Short.MAX_VALUE);
        byteBuf.writeVarLong(this.value);
    }

    @Override
    public void readNetData(@NotNull RegistryFriendlyByteBuf byteBuf) {
        super.readNetData(byteBuf);
        this.key = byteBuf.readUtf(Short.MAX_VALUE);
        this.value = byteBuf.readVarLong();
    }

    @Override
    @NotNull
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @NotNull
    public MutableComponent getButtonText() {
        return Component.translatable(String.valueOf(this.value));
    }

    @Override
    public void fillConfigGroup(@NotNull EditableConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("key", this.key, value -> this.key = value, this.key).setNameKey("extraquests.key_value.key");
        config.addLong("value", this.value, value -> this.value = value, 100L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
    }

    public void progress(@NotNull TeamData teamData, @NotNull String key, long value, boolean ignore) {
        if (this.key.equals(key) && value > 0L && !teamData.isCompleted(this)
                && teamData.getMilliSecondsUntilRepeatable(getQuest()) == 0L
                && (ignore || checkTaskSequence(teamData) && teamData.canStartTasks(getQuest()))) {
            val progress = teamData.getProgress(this);
            val maxProgress = getMaxProgress();

            if (progress < maxProgress) {
                teamData.addProgress(this, Math.min(value, maxProgress - progress));
            }
        }
    }
}
