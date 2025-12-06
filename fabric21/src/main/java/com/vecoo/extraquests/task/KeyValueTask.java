package com.vecoo.extraquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class KeyValueTask extends Task {
    public static TaskType TYPE;

    private String key;
    private long value;

    public KeyValueTask(long id, Quest quest) {
        super(id, quest);
        this.key = "key";
        this.value = 100L;
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
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putString("key", this.key);
        nbt.putLong("value", this.value);
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        this.key = nbt.getString("key");
        this.value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.key, Short.MAX_VALUE);
        buffer.writeVarLong(this.value);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.key = buffer.readUtf(Short.MAX_VALUE);
        this.value = buffer.readVarLong();
    }

    public String key() {
        return this.key;
    }

    public long value() {
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
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("key", this.key, value -> this.key = value, this.key).setNameKey("extraquests.key_value.key");
        config.addLong("value", this.value, value -> this.value = value, 100L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
    }

    public void progress(TeamData teamData, String key, long value, boolean ignore) {
        if (this.key.equals(key) && !teamData.isCompleted(this)) {
            if (ignore) {
                teamData.addProgress(this, value);
            } else if (checkTaskSequence(teamData) && teamData.canStartTasks(getQuest())) {
                teamData.addProgress(this, value);
            }
        }
    }
}