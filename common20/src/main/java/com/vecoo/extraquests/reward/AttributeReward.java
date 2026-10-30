package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.runtime.AttributeBonuses;
import com.vecoo.extraquests.util.FeatureOptions;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import lombok.val;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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

    public boolean canClaim(@NotNull ServerPlayer player) {
        return AttributeBonuses.canGrant(player, this.id, this.attribute, this.amount, this.maximum);
    }

    @Override
    public void claim(@NotNull ServerPlayer player, boolean notify) {
        AttributeBonuses.grant(player, this.id, this.attribute, this.amount, this.maximum, this.operation, this.stacking, this.duration);
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
        this.amount = Double.isFinite(tag.getDouble("amount")) ? Math.max(-1024D, Math.min(1024D, tag.getDouble("amount"))) : 0D;
        this.maximum = Double.isFinite(tag.getDouble("maximum")) ? Math.max(0D, Math.min(1024D, tag.getDouble("maximum"))) : 20D;
        this.operation = Math.max(0, Math.min(2, tag.getInt("operation")));
        this.stacking = Math.max(0, Math.min(2, tag.getInt("stacking")));
        this.duration = Math.max(0L, Math.min(31536000L, tag.getLong("duration")));
    }

    @Override
    public void writeData(@NotNull CompoundTag tag) {
        super.writeData(tag);
        writeSettings(tag);
    }

    @Override
    public void readData(@NotNull CompoundTag tag) {
        super.readData(tag);
        readSettings(tag);
    }

    @Override
    public void writeNetData(@NotNull FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        val tag = new CompoundTag();

        writeSettings(tag);
        buffer.writeNbt(tag);
    }

    @Override
    public void readNetData(@NotNull FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        val tag = buffer.readNbt();

        readSettings(tag == null ? new CompoundTag() : tag);
    }

    @Override
    public void fillConfigGroup(@NotNull ConfigGroup config) {
        super.fillConfigGroup(config);
        val attributes = NameMap.of(new ResourceLocation("minecraft:generic.max_health"), BuiltInRegistries.ATTRIBUTE.keySet().stream().sorted().toList())
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
