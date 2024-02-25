package com.vecoo.extraquests.task;

import com.prototype.economy.EconomyMod;
import com.prototype.economy.api.Account;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ISingleLongValueTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EconomyTask extends Task implements ISingleLongValueTask {
    public static TaskType TYPE;

    public long money = 100L;

    public EconomyTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public long getMaxProgress() {
        return money;
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
    public void setValue(long v) {
        money = v;
    }

    @Override
    public boolean consumesResources() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.economy.title", getMoney());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("money", money, v -> money = v, 100L, 1L, Long.MAX_VALUE).setNameKey("extraquests.economy.money");
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
        if (!checkTaskSequence(teamData) || teamData.isCompleted(this)) {
            return;
        }

        Account account = EconomyMod.getEconomy().getAccount(player.getName().getString());
        int add = (int) Math.min(account.getBalance(), Math.min(money - teamData.getProgress(this), Integer.MAX_VALUE));

        if (add <= 0) {
            return;
        }

        EconomyMod.getEconomy().withdraw(account, add);
        teamData.addProgress(this, add);
    }
}