package com.vecoo.extraquests.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(CommandReward.class)
public abstract class CommandRewardMixin extends Reward {
    @Shadow
    private String command;
    @Shadow
    private boolean elevatePerms;
    @Shadow
    private boolean silent;
    private boolean console;

    public CommandRewardMixin(long id, Quest q) {
        super(id, q);
    }

    /**
     * @author Vecoo
     * @reason Add boolean "console"
     */
    @Overwrite(remap = false)
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("command", command);
        if (elevatePerms) {
            nbt.putBoolean("elevate_perms", true);
        }
        if (silent) {
            nbt.putBoolean("silent", true);
        }
        if (console) {
            nbt.putBoolean("console", true);
        }
    }

    /**
     * @author Vecoo
     * @reason Add boolean "console"
     */
    @Overwrite(remap = false)
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        command = nbt.getString("command");
        elevatePerms = nbt.getBoolean("elevate_perms");
        silent = nbt.getBoolean("silent");
        console = nbt.getBoolean("console");
    }

    /**
     * @author Vecoo
     * @reason Add boolean "console"
     */
    @Overwrite(remap = false)
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(command, Short.MAX_VALUE);
        buffer.writeBoolean(elevatePerms);
        buffer.writeBoolean(silent);
        buffer.writeBoolean(console);
    }

    /**
     * @author Vecoo
     * @reason Add boolean "console"
     */
    @Overwrite(remap = false)
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        command = buffer.readUtf(Short.MAX_VALUE);
        elevatePerms = buffer.readBoolean();
        silent = buffer.readBoolean();
        console = buffer.readBoolean();
    }

    /**
     * @author Vecoo
     * @reason Add boolean "console"
     */
    @Overwrite(remap = false)
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("command", command, v -> command = v, "say Hi, @team!").setNameKey("ftbquests.reward.ftbquests.command");
        config.addBool("elevate", elevatePerms, v -> elevatePerms = v, false);
        config.addBool("silent", silent, v -> silent = v, false);
        config.addBool("console", console, v -> console = v, false).setNameKey("extraquests.reward.mixins.console");
    }

    /**
     * @author Vecoo
     * @reason Add boolean "console"
     */

    @Overwrite(remap = false)
    public void claim(ServerPlayer player, boolean notify) {
        Map<String, Object> overrides = new HashMap<>();

        BlockPos pos = player.blockPosition();
        overrides.put("x", pos.getX());
        overrides.put("y", pos.getY());
        overrides.put("z", pos.getZ());

        if (getQuestChapter() != null) {
            overrides.put("chapter", getQuestChapter());
        }

        overrides.put("quest", quest);

        String cmd = command;
        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            if (entry.getValue() != null) {
                cmd = cmd.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }

        CommandSourceStack source = player.createCommandSourceStack();
        if (elevatePerms) {
            source = source.withPermission(2);
        }
        if (silent) {
            source = source.withSuppressedOutput();
        }
        if (console) {
            player.server.getCommands().performPrefixedCommand(source.getServer().createCommandSourceStack(), cmd);
        } else {
            player.server.getCommands().performPrefixedCommand(source, cmd);
        }
    }
}