package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.api.service.ExtraQuestsService;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Getter
public class TimerReward extends Reward {
    public static RewardType TYPE;

    private String questID;
    private int time;

    public TimerReward(Quest quest) {
        super(quest);
        this.questID = "";
        this.time = 300;
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("quest", this.questID);
        nbt.putInt("time", this.time);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.questID = nbt.getString("quest");
        this.time = nbt.getInt("time");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.questID);
        buffer.writeVarInt(this.time);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.questID = buffer.readUtf();
        this.time = buffer.readVarInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.timer.title", this.time);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("questID", this.questID, value -> this.questID = value, "").setNameKey("extraquests.timer.questid");
        config.addInt("time", this.time, value -> this.time = value, 300, 1, Integer.MAX_VALUE).setNameKey("extraquests.timer.time");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        if (this.questID.isEmpty()) {
            ExtraQuestsService.addQuestTimer(player.getUUID(), quest.getCodeString(), this.time);
        } else {
            ExtraQuestsService.addQuestTimer(player.getUUID(), this.questID, this.time);
        }
    }
}