package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.timer.QuestTimer;
import com.vecoo.extraquests.timer.QuestTimerFactory;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TimerReward extends Reward {
    public static RewardType TYPE;

    private long time = 300L;

    public TimerReward(Quest quest) {
        super(quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundNBT nbt) {
        super.writeData(nbt);
        nbt.putLong("time", time);
    }

    @Override
    public void readData(CompoundNBT nbt) {
        super.readData(nbt);
        time = nbt.getLong("time");
    }

    @Override
    public void writeNetData(PacketBuffer buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(time);
    }

    @Override
    public void readNetData(PacketBuffer buffer) {
        super.readNetData(buffer);
        time = buffer.readVarLong();
    }

    public long getTime() {
        return time;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addLong("time", time, v -> time = v, 300L, 1L, Long.MAX_VALUE).setNameKey("extraquests.timer.time");
    }

    @Override
    public void claim(ServerPlayerEntity player, boolean notify) {
        QuestTimerFactory.addQuestTimer(new QuestTimer(player.getUUID(), quest.getCodeString(), time));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IFormattableTextComponent getAltTitle() {
        return new TranslationTextComponent("extraquests.timer.title", getTime());
    }
}