package com.vecoo.extraquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KeyValueTask extends Task {
    public static TaskType TYPE;

    private String key = "key";
    private long value = 100L;

    public KeyValueTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public long getMaxProgress() {
        return value;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("key", key);
        nbt.putLong("value", value);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        key = nbt.getString("key");
        value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(key, Short.MAX_VALUE);
        buffer.writeVarLong(value);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        key = buffer.readUtf(Short.MAX_VALUE);
        value = buffer.readVarLong();
    }

    public String getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", getKey(), getValue());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("key", key, v -> key = v, key).setNameKey("extraquests.key_value.key");
        config.addLong("value", value, v -> value = v, 100L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
    }

    public void progress(TeamData teamData, String key, long value) {
        if (!teamData.isCompleted(this) && this.key.equals(key)) {
            teamData.addProgress(this, value);
        }
    }
}