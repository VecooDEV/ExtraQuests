package com.vecoo.extraquests.reward;

import com.prototype.economy.EconomyMod;
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

public class EconomyReward extends Reward {
    public static RewardType TYPE;

    private long money = 100L;

    public EconomyReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putLong("money", money);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        money = nbt.getLong("money");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(money);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        money = buffer.readVarLong();
    }

    public long getMoney() {
        return money;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("money", money, v -> money = v, 100L, 1L, Long.MAX_VALUE).setNameKey("extraquests.economy.money");
        ;
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        EconomyMod.getEconomy().deposit(EconomyMod.getEconomy().getAccount(player.getName().getString()), (int) money);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.economy.title", getMoney());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getButtonText() {
        return "+" + money;
    }
}