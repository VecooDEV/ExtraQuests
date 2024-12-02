package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.storage.quests.QuestTimer;
import com.vecoo.extraquests.storage.QuestsFactory;
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

    private String quest = "";
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
        nbt.putString("quest", quest);
        nbt.putLong("time", time);
    }

    @Override
    public void readData(CompoundNBT nbt) {
        super.readData(nbt);
        quest = nbt.getString("quest");
        time = nbt.getLong("time");
    }

    @Override
    public void writeNetData(PacketBuffer buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(quest);
        buffer.writeVarLong(time);
    }

    @Override
    public void readNetData(PacketBuffer buffer) {
        super.readNetData(buffer);
        quest = buffer.readUtf();
        time = buffer.readVarLong();
    }

    public String getQuest() {
        return this.quest;
    }

    public long getTime() {
        return this.time;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addLong("time", time, v -> time = v, 300L, 1L, Long.MAX_VALUE).setNameKey("extraquests.timer.time");
    }

    @Override
    public void claim(ServerPlayerEntity player, boolean notify) {
        QuestsFactory.addQuestTimer(new QuestTimer(player.getUUID(), quest.getCodeString(), time));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IFormattableTextComponent getAltTitle() {
        return new TranslationTextComponent("extraquests.timer.title", getTime());
    }
}