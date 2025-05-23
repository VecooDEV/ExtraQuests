package com.vecoo.extraquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public void writeData(CompoundNBT nbt) {
        super.writeData(nbt);
        nbt.putString("key", this.key);
        nbt.putLong("value", this.value);
    }

    @Override
    public void readData(CompoundNBT nbt) {
        super.readData(nbt);
        this.key = nbt.getString("key");
        this.value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(PacketBuffer buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.key, Short.MAX_VALUE);
        buffer.writeVarLong(this.value);
    }

    @Override
    public void readNetData(PacketBuffer buffer) {
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
    @OnlyIn(Dist.CLIENT)
    public IFormattableTextComponent getAltTitle() {
        return new TranslationTextComponent("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IFormattableTextComponent getButtonText() {
        return new StringTextComponent(String.valueOf(this.value));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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