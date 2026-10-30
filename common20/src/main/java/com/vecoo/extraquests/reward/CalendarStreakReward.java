package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.quest.QuestSettings;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import lombok.val;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class CalendarStreakReward extends Reward {
    public static RewardType TYPE;

    private ItemStack item = new ItemStack(Items.IRON_INGOT, 5);
    private ItemStack bonus = new ItemStack(Items.DIAMOND);
    private int every = 7;

    public CalendarStreakReward(long id, @NotNull Quest quest) {
        super(id, quest);
    }

    @Override
    @NotNull
    public RewardType getType() {
        return TYPE;
    }

    public boolean canClaim(@NotNull TeamData data) {
        val calendar = QuestSettings.get(this.quest);
        val file = ServerQuestFile.INSTANCE;

        return calendar.isCalendarEnabled() && file != null && data.getFile().isServerSide()
                && calendar.getStreak(this.quest, data, file.server) > 0;
    }

    @Override
    public void claim(@NotNull ServerPlayer player, boolean notify) {
        val calendar = QuestSettings.get(this.quest);

        if (!calendar.isCalendarEnabled()) {
            return;
        }

        val data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);

        if (data == null) {
            return;
        }

        val streak = calendar.getStreak(this.quest, data, player.server);

        if (streak <= 0) {
            return;
        }

        player.getInventory().placeItemBackInInventory(this.item.copy());
        if (streak % this.every == 0) {
            player.getInventory().placeItemBackInInventory(this.bonus.copy());
        }

        return;
    }

    @Override
    public void writeData(@NotNull CompoundTag tag) {
        super.writeData(tag);
        tag.put("item", this.item.save(new CompoundTag()));
        tag.put("bonus", this.bonus.save(new CompoundTag()));
        tag.putInt("every", this.every);
    }

    @Override
    public void readData(@NotNull CompoundTag tag) {
        super.readData(tag);
        this.item = ItemStack.of(tag.getCompound("item"));
        this.bonus = ItemStack.of(tag.getCompound("bonus"));
        this.every = Math.max(1, tag.getInt("every"));
    }

    @Override
    public void writeNetData(@NotNull FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeItem(this.item);
        buffer.writeItem(this.bonus);
        buffer.writeVarInt(this.every);
    }

    @Override
    public void readNetData(@NotNull FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.item = buffer.readItem();
        this.bonus = buffer.readItem();
        this.every = Math.max(1, buffer.readVarInt());
    }

    @Override
    public void fillConfigGroup(@NotNull ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addItemStack("item", this.item, value -> this.item = value, new ItemStack(Items.IRON_INGOT, 5), true, true).setNameKey("extraquests.streak.item");
        config.addItemStack("bonus", this.bonus, value -> this.bonus = value, new ItemStack(Items.DIAMOND), true, true).setNameKey("extraquests.streak.bonus");
        config.addInt("every", this.every, value -> this.every = value, 7, 1, Integer.MAX_VALUE).setNameKey("extraquests.streak.every");
    }
}
