package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.api.factory.QuestsFactory;
import com.vecoo.extraquests.storage.quests.QuestTimer;
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

    private String questID = "";
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
        nbt.putString("quest", questID);
        nbt.putLong("time", time);
    }

    @Override
    public void readData(CompoundNBT nbt) {
        super.readData(nbt);
        questID = nbt.getString("quest");
        time = nbt.getLong("time");
    }

    @Override
    public void writeNetData(PacketBuffer buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(questID);
        buffer.writeVarLong(time);
    }

    @Override
    public void readNetData(PacketBuffer buffer) {
        super.readNetData(buffer);
        questID = buffer.readUtf();
        time = buffer.readVarLong();
    }

    public String getQuestID() {
        return this.questID;
    }

    public long getTime() {
        return this.time;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("questID", this.questID, v -> this.questID = v, "").setNameKey("extraquests.timer.questid");
        config.addLong("time", time, v -> time = v, 300L, 1L, Long.MAX_VALUE).setNameKey("extraquests.timer.time");
    }

    @Override
    public void claim(ServerPlayerEntity player, boolean notify) {
        if (this.questID.isEmpty()) {
            QuestsFactory.addQuestTimer(new QuestTimer(player.getUUID(), quest.getCodeString(), this.time));
        } else {
            QuestsFactory.addQuestTimer(new QuestTimer(player.getUUID(), this.questID, this.time));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IFormattableTextComponent getAltTitle() {
        return new TranslationTextComponent("extraquests.timer.title", getTime());
    }
}