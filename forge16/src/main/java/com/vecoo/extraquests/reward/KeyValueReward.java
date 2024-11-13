package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
    public void writeData(CompoundNBT nbt) {
        super.writeData(nbt);
        nbt.putString("key", key);
        nbt.putLong("value", value);
    }

    @Override
    public void readData(CompoundNBT nbt) {
        super.readData(nbt);
        key = nbt.getString("key");
        value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(PacketBuffer buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(key, Short.MAX_VALUE);
        buffer.writeVarLong(value);
    }

    @Override
    public void readNetData(PacketBuffer buffer) {
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
    public void claim(ServerPlayerEntity player, boolean notify) {
        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getData(player), key, value);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IFormattableTextComponent getAltTitle() {
        return new TranslationTextComponent("extraquests.key_value.title", getKey(), getValue());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getButtonText() {
        return "+" + value;
    }
}