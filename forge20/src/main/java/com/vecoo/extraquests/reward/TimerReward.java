package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.api.factory.QuestsFactory;
import com.vecoo.extraquests.storage.quests.QuestTimer;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TimerReward extends Reward {
    public static RewardType TYPE;

    private String questID = "";
    private long time = 300L;

    public TimerReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("quest", questID);
        nbt.putLong("time", time);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        questID = nbt.getString("quest");
        time = nbt.getLong("time");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(questID);
        buffer.writeVarLong(time);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
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
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("questID", this.questID, v -> this.questID = v, "").setNameKey("extraquests.timer.questid");
        config.addLong("time", this.time, v -> this.time = v, 300L, 1L, Long.MAX_VALUE).setNameKey("extraquests.timer.time");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        if (this.questID.isEmpty()) {
            QuestsFactory.addQuestTimer(new QuestTimer(player.getUUID(), getQuest().getCodeString(), this.time));
        } else {
            QuestsFactory.addQuestTimer(new QuestTimer(player.getUUID(), this.questID, this.time));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.timer.title", getTime());
    }
}