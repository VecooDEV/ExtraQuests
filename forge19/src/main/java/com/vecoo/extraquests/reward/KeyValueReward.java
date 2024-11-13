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
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("key", key, v -> key = v, key).setNameKey("extraquests.key_value.key");
        config.addLong("value", value, v -> value = v, 5L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getData(player), key, value);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", getKey(), getValue());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getButtonText() {
        return "+" + value;
    }
}