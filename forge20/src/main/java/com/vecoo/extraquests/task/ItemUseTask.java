package com.vecoo.extraquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.quests.ValidItemsScreen;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NBTUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemUseTask extends Task implements Predicate<ItemStack> {
    public static TaskType TYPE;

    private ItemStack itemStack = ItemStack.EMPTY;
    private long amount = 1L;

    public ItemUseTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public long getMaxProgress() {
        return amount;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        NBTUtils.write(nbt, "item", itemStack);
        nbt.putLong("amount", amount);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        itemStack = NBTUtils.read(nbt, "item");
        amount = nbt.getLong("amount");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        FTBQuestsNetHandler.writeItemType(buffer, itemStack);
        buffer.writeLong(amount);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        itemStack = FTBQuestsNetHandler.readItemType(buffer);
        amount = buffer.readVarLong();
    }

    @Override
    public boolean test(ItemStack stack) {
        if (itemStack.isEmpty()) {
            return true;
        }

        return ItemMatchingSystem.INSTANCE.doesItemMatch(itemStack, stack, false, false);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public long getAmount() {
        return amount;
    }

    public List<ItemStack> getValidDisplayItems() {
        return ItemMatchingSystem.INSTANCE.getAllMatchingStacks(itemStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", getItemStack().getHoverName(), getAmount());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Icon getAltIcon() {
        List<Icon> icons = new ArrayList<>();

        for (ItemStack stack : getValidDisplayItems()) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            Icon icon = ItemIcon.getItemIcon(copy);

            if (!icon.isEmpty()) {
                icons.add(icon);
            }
        }

        if (icons.isEmpty()) {
            return ItemIcon.getItemIcon(Items.ACACIA_PLANKS);
        }

        return IconAnimation.fromList(icons, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onButtonClicked(Button button, boolean canClick) {
        button.playClickSound();

        List<ItemStack> validItems = getValidDisplayItems();

        if (!consumesResources() && validItems.size() == 1 && FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
            FTBQuests.getRecipeModHelper().showRecipes(validItems.get(0));
        } else if (validItems.isEmpty()) {
            Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.literal("No valid items!"), ItemIcon.getItemIcon(Items.ACACIA_PLANKS), Component.literal("Report this bug to modpack author!")));
        } else {
            new ValidItemsScreen(null, validItems, canClick).openGui();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addItemStack("item", itemStack, v -> itemStack = v, ItemStack.EMPTY, true, false).setNameKey("extraquests.key_value.key");
        config.addLong("amount", amount, v -> amount = v, 1, 1, Long.MAX_VALUE);
    }

    public void progress(TeamData teamData, ItemStack itemStack) {
        if (teamData.isCompleted(this) || !checkTaskSequence(teamData) || !teamData.canStartTasks(getQuest())) {
            return;
        }

        if (!(itemStack.getItem() instanceof MissingItem) && !itemStack.is(getItemStack().getItem())) {
            return;
        }

        teamData.addProgress(this, 1L);
    }
}
