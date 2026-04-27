package com.vecoo.extraquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ISingleLongValueTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@Getter
public class KeyValueTask extends Task implements ISingleLongValueTask {
    public static TaskType TYPE;

    private String key;
    @Setter
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
    public void writeData(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.writeData(compoundTag, provider);
        compoundTag.putString("key", this.key);
        compoundTag.putLong("value", this.value);
    }

    @Override
    public void readData(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.readData(compoundTag, provider);
        this.key = compoundTag.getString("key");
        this.value = compoundTag.getLong("value");
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf byteBuf) {
        super.writeNetData(byteBuf);
        byteBuf.writeUtf(this.key, Short.MAX_VALUE);
        byteBuf.writeVarLong(this.value);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf byteBuf) {
        super.readNetData(byteBuf);
        this.key = byteBuf.readUtf(Short.MAX_VALUE);
        this.value = byteBuf.readVarLong();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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