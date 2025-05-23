package com.vecoo.extraquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class KeyValueTask extends Task {
    public static TaskType TYPE;

    private String key = "key";
    private long value = 100L;

    public KeyValueTask(Quest quest) {
        super(quest);
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public long getMaxProgress() {
        return this.value;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("key", this.key);
        nbt.putLong("value", this.value);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.key = nbt.getString("key");
        this.value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.key, Short.MAX_VALUE);
        buffer.writeVarLong(this.value);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.key = buffer.readUtf(Short.MAX_VALUE);
        this.value = buffer.readVarLong();
    }

    public String getKey() {
        return this.key;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public MutableComponent getButtonText() {
        return Component.translatable(String.valueOf(this.value));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("key", this.key, v -> this.key = v, this.key).setNameKey("extraquests.key_value.key");
        config.addLong("value", this.value, v -> this.value = v, 100L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
    }


    public void progress(TeamData teamData, String key, long value) {
        if (!this.key.equals(key)) {
            return;
        }

        if (teamData.isCompleted(this) || !teamData.canStartTasks(this.quest)) {
            return;
        }

        teamData.addProgress(this, value);
    }
}