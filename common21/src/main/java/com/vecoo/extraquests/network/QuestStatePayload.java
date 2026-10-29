package com.vecoo.extraquests.network;

import com.vecoo.extraquests.ExtraQuestsConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record QuestStatePayload(@NotNull CompoundTag data) implements CustomPacketPayload {
    public static final Type<QuestStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtraQuestsConstants.MOD_ID, "quest_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, QuestStatePayload::data, QuestStatePayload::new);

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
