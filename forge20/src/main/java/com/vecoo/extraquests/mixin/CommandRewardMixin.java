package com.vecoo.extraquests.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = CommandReward.class, remap = false)
public abstract class CommandRewardMixin extends Reward {

    @Shadow
    private String command;

    @Shadow
    private boolean elevatePerms;

    @Shadow
    private boolean silent;

    @Unique
    private boolean console = false;

    public CommandRewardMixin(long id, Quest q) {
        super(id, q);
    }

    @Inject(method = "writeData", at = @At("RETURN"))
    public void writeData(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("console", console);
    }

    @Inject(method = "readData", at = @At("RETURN"))
    public void readData(CompoundTag nbt, CallbackInfo ci) {
        console = nbt.getBoolean("console");
    }

    @Inject(method = "writeNetData", at = @At("RETURN"))
    public void writeNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(console);
    }

    @Inject(method = "readNetData", at = @At("RETURN"))
    public void readNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        console = buffer.readBoolean();
    }

    @Inject(method = "fillConfigGroup", at = @At("RETURN"))
    public void fillConfigGroup(ConfigGroup config, CallbackInfo ci) {
        config.addBool("console", console, v -> console = v, false).setNameKey("extraquests.reward.mixins.console");
    }

    /**
     * @author Vecoo
     * @reason Add new options
     */
    @Overwrite(remap = false)
    public void claim(ServerPlayer player, boolean notify) {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("p", player.getGameProfile().getName());

        BlockPos pos = player.blockPosition();
        overrides.put("x", pos.getX());
        overrides.put("y", pos.getY());
        overrides.put("z", pos.getZ());

        if (getQuestChapter() != null) {
            overrides.put("chapter", getQuestChapter());
        }

        overrides.put("quest", quest);
        overrides.put("team", FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                .map(team -> team.getName().getString())
                .orElse(player.getGameProfile().getName())
        );

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