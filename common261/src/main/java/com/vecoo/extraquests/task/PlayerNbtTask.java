package com.vecoo.extraquests.task;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vecoo.extraquests.client.config.PlayerNbtConfig;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PlayerNbtTask extends Task {
    public static TaskType TYPE;

    @NotNull
    private CompoundTag nbt = new CompoundTag();
    private boolean exactMatch = false;

    public PlayerNbtTask(long id, @NotNull Quest quest) {
        super(id, quest);
    }

    @Override
    @NotNull
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 60;
    }

    public boolean matches(@NotNull CompoundTag compoundTag) {
        return !this.nbt.isEmpty() && (this.exactMatch ? this.nbt.equals(compoundTag) : NbtUtils.compareNbt(this.nbt, compoundTag, true));
    }

    @Override
    public void submitTask(@NotNull TeamData teamData, @NotNull ServerPlayer player, @NotNull ItemStack itemStack) {
        if (this.nbt.isEmpty() || teamData.isLocked() || teamData.isCompleted(this) || !checkTaskSequence(teamData) || !teamData.canStartTasks(getQuest())) {
            return;
        }

        val file = ServerQuestFile.exists() ? ServerQuestFile.getInstance() : null;

        if (file != null && teamData.getFile().isServerSide()) {
            val output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());

            player.saveWithoutId(output);
            if (matches(output.buildResult())) {
                file.withPlayerContext(player, () -> teamData.setProgress(this, 1L));
            }
        }
    }

    @Override
    public void writeData(@NotNull Json5Object compoundTag, @NotNull HolderLookup.Provider provider) {
        super.writeData(compoundTag, provider);
        compoundTag.addProperty("nbt", this.nbt.toString());
        compoundTag.addProperty("exact_match", this.exactMatch);
    }

    @Override
    public void readData(@NotNull Json5Object compoundTag, @NotNull HolderLookup.Provider provider) {
        super.readData(compoundTag, provider);

        try {
            this.nbt = TagParser.parseCompoundFully(Json5Util.getString(compoundTag, "nbt").orElse("{}"));
        } catch (CommandSyntaxException e) {
            this.nbt = new CompoundTag();
            ExtraQuestsLogger.getLogger().warn("Cannot read NBT filter for task {}.", Long.toUnsignedString(this.id, 16), e);
        }

        this.exactMatch = Json5Util.getBoolean(compoundTag, "exact_match").orElse(false);
    }

    @Override
    public void writeNetData(@NotNull RegistryFriendlyByteBuf byteBuf) {
        super.writeNetData(byteBuf);
        byteBuf.writeNbt(this.nbt);
        byteBuf.writeBoolean(this.exactMatch);
    }

    @Override
    public void readNetData(@NotNull RegistryFriendlyByteBuf byteBuf) {
        super.readNetData(byteBuf);
        val nbt = byteBuf.readNbt();

        this.nbt = nbt == null ? new CompoundTag() : nbt;
        this.exactMatch = byteBuf.readBoolean();
    }

    @Override
    public void fillConfigGroup(@NotNull EditableConfigGroup config) {
        super.fillConfigGroup(config);
        config.add("nbt", new PlayerNbtConfig(), this.nbt, this::setNbt, new CompoundTag()).setNameKey("extraquests.player_nbt.nbt");
        config.addBool("exact_match", this.exactMatch, this::setExactMatch, false).setNameKey("extraquests.player_nbt.exact_match");
    }
}
