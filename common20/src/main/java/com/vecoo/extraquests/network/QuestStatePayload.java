package com.vecoo.extraquests.network;

import com.vecoo.extraquests.ExtraQuestsConstants;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import lombok.val;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class QuestStatePayload extends BaseS2CMessage {
    private static MessageType type;

    private final CompoundTag data;

    public QuestStatePayload(@NotNull CompoundTag data) {
        this.data = data;
    }

    public QuestStatePayload(@NotNull FriendlyByteBuf buffer) {
        val data = buffer.readNbt();

        this.data = data == null ? new CompoundTag() : data;
    }

    public static void register() {
        type = SimpleNetworkManager.create(ExtraQuestsConstants.MOD_ID).registerS2C("quest_state", QuestStatePayload::new);
    }

    @Override
    @NotNull
    public MessageType getType() {
        return type;
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeNbt(this.data);
    }

    @Override
    public void handle(@NotNull NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() != null) {
                QuestClientState.update(this.data);
            }
        });
    }
}
