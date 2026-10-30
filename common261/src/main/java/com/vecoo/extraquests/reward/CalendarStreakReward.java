package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.quest.QuestSettings;
import com.vecoo.extraquests.util.QuestJsonOps;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import lombok.val;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    @Override
    public boolean claim(@NotNull ServerPlayer player, boolean notify) {
        val calendar = QuestSettings.get(this.quest);

        if (!calendar.isCalendarEnabled()) {
            return false;
        }

        val data = ServerQuestFile.getInstance().getTeamData(player).orElse(null);

        if (data == null) {
            return false;
        }

        val streak = calendar.getStreak(this.quest, data, player.level().getServer());

        if (streak <= 0) {
            return false;
        }

        player.getInventory().placeItemBackInInventory(this.item.copy());
        if (streak % this.every == 0) {
            player.getInventory().placeItemBackInInventory(this.bonus.copy());
        }

        return true;
    }

    @Override
    public void writeData(@NotNull Json5Object tag, @NotNull HolderLookup.Provider provider) {
        super.writeData(tag, provider);
        val ops = provider.createSerializationContext(QuestJsonOps.INSTANCE);

        tag.add("item", ItemStack.OPTIONAL_CODEC.encodeStart(ops, this.item).getOrThrow());
        tag.add("bonus", ItemStack.OPTIONAL_CODEC.encodeStart(ops, this.bonus).getOrThrow());
        tag.addProperty("every", this.every);
    }

    @Override
    public void readData(@NotNull Json5Object tag, @NotNull HolderLookup.Provider provider) {
        super.readData(tag, provider);
        this.item = Json5Util.fetch(tag, provider, "item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.bonus = Json5Util.fetch(tag, provider, "bonus", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.every = Math.max(1, Json5Util.getInt(tag, "every").orElse(0));
    }

    @Override
    public void writeNetData(@NotNull RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, this.item);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, this.bonus);
        buffer.writeVarInt(this.every);
    }

    @Override
    public void readNetData(@NotNull RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        this.bonus = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        this.every = Math.max(1, buffer.readVarInt());
    }

    @Override
    public void fillConfigGroup(@NotNull EditableConfigGroup config) {
        super.fillConfigGroup(config);
        config.addItemStack("item", this.item, value -> this.item = value, new ItemStack(Items.IRON_INGOT, 5), true, true).setNameKey("extraquests.streak.item");
        config.addItemStack("bonus", this.bonus, value -> this.bonus = value, new ItemStack(Items.DIAMOND), true, true).setNameKey("extraquests.streak.bonus");
        config.addInt("every", this.every, value -> this.every = value, 7, 1, Integer.MAX_VALUE).setNameKey("extraquests.streak.every");
    }
}
