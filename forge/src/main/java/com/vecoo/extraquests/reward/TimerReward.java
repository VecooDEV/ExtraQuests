package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.timer.QuestTimerListing;
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

    private long time = 5L;

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
        nbt.putLong("time", time);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        time = nbt.getLong("time");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(time);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        time = buffer.readVarLong();
    }

    public long getTime() {
        return time;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("time", time, v -> time = v, 5L, 1L, Long.MAX_VALUE).setNameKey("extraquests.timer.time");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        QuestTimerListing listing = new QuestTimerListing(player.getUUID(), getQuest(), time);
        ExtraQuests.getInstance().getListingsProvider().addListing(listing);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.timer.title", getTime());
    }
}