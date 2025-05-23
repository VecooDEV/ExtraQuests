package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class KeyValueReward extends Reward {
    public static RewardType TYPE;

    private String key = "key";
    private long value = 5L;

    public KeyValueReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
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
    public String getButtonText() {
        return "+" + this.value;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("key", this.key, v -> this.key = v, this.key).setNameKey("extraquests.key_value.key");
        config.addLong("value", this.value, v -> this.value = v, 5L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getOrCreateTeamData(player), this.key, this.value);
        }
    }
}