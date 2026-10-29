package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.runtime.AttributeBonuses;
import com.vecoo.extraquests.util.FeatureOptions;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import lombok.val;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class AttributeReward extends Reward {
    public static RewardType TYPE;

    @NotNull
    private String attribute = "minecraft:generic.max_health";
    private double amount = 2D;
    private double maximum = 20D;
    private int operation = 0;
    private int stacking = 0;
    private long duration = 0L;

    public AttributeReward(long id, @NotNull Quest quest) {
        super(id, quest);
    }

    @Override
    @NotNull
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public boolean claim(@NotNull ServerPlayer player, boolean notify) {
        return AttributeBonuses.grant(player, this.id, this.attribute, this.amount, this.maximum, this.operation, this.stacking, this.duration);
    }

    private void writeSettings(@NotNull CompoundTag tag) {
        tag.putString("attribute", this.attribute);
        tag.putDouble("amount", this.amount);
        tag.putDouble("maximum", this.maximum);
        tag.putInt("operation", this.operation);
        tag.putInt("stacking", this.stacking);
        tag.putLong("duration", this.duration);
    }

    private void readSettings(@NotNull CompoundTag tag) {
        this.attribute = tag.getString("attribute");
        this.amount = Double.isFinite(tag.getDouble("amount")) ? Math.clamp(tag.getDouble("amount"), -1024D, 1024D) : 0D;
        this.maximum = Double.isFinite(tag.getDouble("maximum")) ? Math.clamp(tag.getDouble("maximum"), 0D, 1024D) : 20D;
        this.operation = Math.clamp(tag.getInt("operation"), 0, 2);
        this.stacking = Math.clamp(tag.getInt("stacking"), 0, 2);
        this.duration = Math.clamp(tag.getLong("duration"), 0L, 31536000L);
    }

    @Override
    public void writeData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.writeData(tag, provider);
        writeSettings(tag);
    }

    @Override
    public void readData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.readData(tag, provider);
        readSettings(tag);
    }

    @Override
    public void writeNetData(@NotNull RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        val tag = new CompoundTag();

        writeSettings(tag);
        buffer.writeNbt(tag);
    }

    @Override
    public void readNetData(@NotNull RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        val tag = buffer.readNbt();

        readSettings(tag == null ? new CompoundTag() : tag);
    }

    @Override
    public void fillConfigGroup(@NotNull ConfigGroup config) {
        super.fillConfigGroup(config);
        val attributes = NameMap.of(ResourceLocation.parse("minecraft:generic.max_health"), BuiltInRegistries.ATTRIBUTE.keySet().stream().sorted().toList())
                .id(ResourceLocation::toString).name(id -> Component.translatable(BuiltInRegistries.ATTRIBUTE.get(id).getDescriptionId())).create();
        val selected = ResourceLocation.tryParse(this.attribute);

        config.addEnum("attribute", selected != null && attributes.values.contains(selected) ? selected : attributes.defaultValue,
                value -> this.attribute = value.toString(), attributes).setNameKey("extraquests.attribute.id");
        config.addDouble("amount", this.amount, value -> this.amount = value, 2D, -1024D, 1024D).setNameKey("extraquests.attribute.amount");
        config.addDouble("maximum", this.maximum, value -> this.maximum = value, 20D, 0D, 1024D).setNameKey("extraquests.attribute.maximum");
        config.addEnum("operation", this.operation, value -> this.operation = value, FeatureOptions.OPERATIONS).setNameKey("extraquests.attribute.operation");
        config.addEnum("stacking", this.stacking, value -> this.stacking = value, FeatureOptions.STACKING).setNameKey("extraquests.attribute.stacking");
        config.addLong("duration", this.duration, value -> this.duration = value, 0L, 0L, 31536000L).setNameKey("extraquests.attribute.duration");
    }
}
