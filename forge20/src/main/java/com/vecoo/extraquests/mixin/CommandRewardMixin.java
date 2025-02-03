package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
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

    @Shadow
    public static String format(String template, Map<String, Object> parameters) {
        throw new AssertionError();
    }

    @Unique
    private boolean console;

    public CommandRewardMixin(long id, Quest q) {
        super(id, q);
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    public void writeData(CompoundTag nbt, CallbackInfo ci) {
        if (console) {
            nbt.putBoolean("console", true);
        }
    }

    @Inject(method = "readData", at = @At("TAIL"))
    public void readData(CompoundTag nbt, CallbackInfo ci) {
        console = nbt.getBoolean("console");
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    public void writeNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(console);
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    public void readNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        console = buffer.readBoolean();
    }

    @Inject(method = "fillConfigGroup", at = @At("TAIL"))
    public void fillConfigGroup(ConfigGroup config, CallbackInfo ci) {
        config.addBool("console", console, v -> console = v, false).setNameKey("extraquests.reward.command.console");
    }

    /**
     * @author Vecoo
     * @reason Add execute console.
     */
    @Overwrite
    @Override
    public void claim(ServerPlayer player, boolean notify) {
        if (!console) {
            return;
        }

        for (String blacklistCommand : ExtraQuests.getInstance().getConfig().getBlacklistConsole()) {
            if (command.contains(blacklistCommand)) {
                return;
            }
        }

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
        FTBTeamsAPI.api().getManager().getTeamForPlayer(player).ifPresent(team -> {
            overrides.put("team", team.getName().getString());
            overrides.put("team_id", team.getShortName());
            overrides.put("long_team_id", team.getId().toString());
            overrides.put("member_count", team.getMembers().size());
            overrides.put("online_member_count", team.getOnlineMembers().size());
        });

        String cmd = format(command, overrides);

        CommandSourceStack source = player.createCommandSourceStack();
        if (elevatePerms) source = source.withPermission(2);
        if (silent) source = source.withSuppressedOutput();

        if (console) {
            player.server.getCommands().performPrefixedCommand(source.getServer().createCommandSourceStack(), cmd);
        } else {
            player.server.getCommands().performPrefixedCommand(source, cmd);
        }
    }
}