package com.vecoo.extraquests.reward;

import com.vecoo.extraquests.util.KeyValueUtil;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import lombok.Getter;
import lombok.val;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Getter
public class KeyValueReward extends Reward {
    public static RewardType TYPE;

    @NotNull
    private String key;
    private long value;
    private boolean ignore;

    public KeyValueReward(long id, @NotNull Quest quest) {
        super(id, quest);
        this.key = "key";
        this.value = 5L;
        this.ignore = false;
    }

    @Override
    @NotNull
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(@NotNull Json5Object compoundTag, @NotNull HolderLookup.Provider provider) {
        super.writeData(compoundTag, provider);
        compoundTag.addProperty("key", this.key);
        compoundTag.addProperty("value", this.value);
        compoundTag.addProperty("ignore", this.ignore);
    }

    @Override
    public void readData(@NotNull Json5Object compoundTag, @NotNull HolderLookup.Provider provider) {
        super.readData(compoundTag, provider);
        this.key = Json5Util.getString(compoundTag, "key").orElse("");
        this.value = Json5Util.getLong(compoundTag, "value").orElse(0L);
        this.ignore = Json5Util.getBoolean(compoundTag, "ignore").orElse(false);
    }

    @Override
    public void writeNetData(@NotNull RegistryFriendlyByteBuf byteBuf) {
        super.writeNetData(byteBuf);
        byteBuf.writeUtf(this.key, Short.MAX_VALUE);
        byteBuf.writeVarLong(this.value);
        byteBuf.writeBoolean(this.ignore);
    }

    @Override
    public void readNetData(@NotNull RegistryFriendlyByteBuf byteBuf) {
        super.readNetData(byteBuf);
        this.key = byteBuf.readUtf(Short.MAX_VALUE);
        this.value = byteBuf.readVarLong();
        this.ignore = byteBuf.readBoolean();
    }

    @Override
    @NotNull
    public MutableComponent getAltTitle() {
        return Component.translatable("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @NotNull
    public String getButtonText() {
        return "+" + this.value;
    }

    @Override
    public void fillConfigGroup(@NotNull EditableConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("key", this.key, value -> this.key = value, this.key).setNameKey("extraquests.key_value.key");
        config.addLong("value", this.value, value -> this.value = value, 5L, 1L, Long.MAX_VALUE).setNameKey("extraquests.key_value.value");
        config.addBool("ignore", this.ignore, value -> this.ignore = value, false).setNameKey("extraquests.key_value.ignore");
    }

    @Override
    public boolean claim(@NotNull ServerPlayer player, boolean notify) {
        val data = ServerQuestFile.getInstance().getTeamData(player).orElse(null);

        if (data == null) {
            return false;
        }

        KeyValueUtil.addProgress(player, data, this.key, this.value, this.ignore);

        return true;
    }
}
