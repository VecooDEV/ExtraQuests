package com.vecoo.extraquests.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.vecoo.extraquests.integration.ExtraIntegration;
import com.vecoo.extraquests.timer.QuestTimer;
import com.vecoo.extraquests.timer.QuestTimerFactory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TimerReward extends Reward {
    private long time = 300L;

    public TimerReward(Quest quest) {
        super(quest);
    }

    @Override
    public RewardType getType() {
        return ExtraIntegration.TIMER;
    }

    @Override
    public void writeData(NBTTagCompound nbt) {
        super.writeData(nbt);
        nbt.setLong("time", time);
    }

    @Override
    public void readData(NBTTagCompound nbt) {
        super.readData(nbt);
        time = nbt.getLong("time");
    }

    @Override
    public void writeNetData(DataOut buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(time);
    }

    @Override
    public void readNetData(DataIn buffer) {
        super.readNetData(buffer);
        time = buffer.readVarLong();
    }

    public long getTime() {
        return time;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addLong("time", () -> time, v -> time = v, 300L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("extraquests.timer.time"));
    }

    @Override
    public void claim(EntityPlayerMP player, boolean notify) {
        QuestTimerFactory.addQuestTimer(new QuestTimer(player.getUniqueID(), quest.getCodeString(), time));
    }

    @Override
    public String getAltTitle() {
        return I18n.format("extraquests.timer.title", getTime());
    }
}