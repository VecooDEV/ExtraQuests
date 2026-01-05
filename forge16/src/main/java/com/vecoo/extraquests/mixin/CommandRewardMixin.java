package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import lombok.val;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandReward.class, remap = false)
public abstract class CommandRewardMixin {
    @Shadow
    public String command;

    @Shadow
    public boolean playerCommand;

    @Inject(
            method = "claim",
            at = @At("HEAD"),
            cancellable = true
    )
    public void claim(ServerPlayerEntity player, boolean notify, CallbackInfo ci) {
        if (!this.playerCommand) {
            val serverConfig = ExtraQuests.getInstance().getServerConfig();

            if (serverConfig.isBlacklistConsole()) {
                for (String blacklistCommand : serverConfig.getBlacklistConsoleList()) {
                    if (this.command.contains(blacklistCommand)) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}