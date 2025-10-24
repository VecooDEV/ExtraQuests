package com.vecoo.extraquests.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.vecoo.extraquests.api.factory.ExtraQuestsFactory;
import com.vecoo.extraquests.integration.QuestsIntegration;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class TimerReward extends Reward {
    private String questID;
    private int time;

    public TimerReward(Quest quest) {
        super(quest);
        this.questID = "";
        this.time = 300;
    }

    @Override
    public RewardType getType() {
        return QuestsIntegration.TIMER_REWARD;
    }

    @Override
    public void writeData(@Nonnull NBTTagCompound nbt) {
        super.writeData(nbt);
        nbt.setString("quest", this.questID);
        nbt.setInteger("time", this.time);
    }

    @Override
    public void readData(@Nonnull NBTTagCompound nbt) {
        super.readData(nbt);
        this.questID = nbt.getString("quest");
        this.time = nbt.getInteger("time");
    }

    @Override
    public void writeNetData(@Nonnull DataOut buffer) {
        super.writeNetData(buffer);
        buffer.writeString(this.questID);
        buffer.writeVarInt(this.time);
    }

    @Override
    public void readNetData(@Nonnull DataIn buffer) {
        super.readNetData(buffer);
        this.questID = buffer.readString();
        this.time = buffer.readVarInt();
    }

    public String getQuestID() {
        return this.questID;
    }

    public int getTime() {
        return this.time;
    }

    @Override
    @Nonnull
    public String getAltTitle() {
        return I18n.format("extraquests.timer.title", this.time);
    }

    @Override
    public void getConfig(@Nonnull ConfigGroup config) {
        super.getConfig(config);
        config.addString("questID", () -> this.questID, value -> this.questID = value, "").setDisplayName(new TextComponentTranslation("extraquests.timer.questid"));
        config.addInt("time", () -> this.time, value -> this.time = value, 300, 1, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("extraquests.timer.time"));
    }

    @Override
    public void claim(@Nonnull EntityPlayerMP player, boolean notify) {
        if (this.questID.isEmpty()) {
            ExtraQuestsFactory.TimerProvider.addTimerQuests(player.getUniqueID(), quest.getCodeString(), this.time);
        } else {
            ExtraQuestsFactory.TimerProvider.addTimerQuests(player.getUniqueID(), this.questID, this.time);
        }
    }
}