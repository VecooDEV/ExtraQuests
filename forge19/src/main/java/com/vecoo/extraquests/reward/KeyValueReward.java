package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KeyValueReward extends Reward {
    public static RewardType TYPE;

    private String key = "key";
    private long value = 5L;

    public KeyValueReward(Quest quest) {
        super(quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
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
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getButtonText() {
        return "+" + this.value;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("key", this.key, v -> this.key = v, this.key).setNameKey("extraquests.key_value.key");
        config.addLong("value", this.value, v -> this.value = v, 5L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getData(player), this.key, this.value);
        }
    }
}