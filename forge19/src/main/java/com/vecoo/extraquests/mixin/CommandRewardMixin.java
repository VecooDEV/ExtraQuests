package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandReward.class, remap = false)
public abstract class CommandRewardMixin extends Reward {
    public CommandRewardMixin(Quest quest) {
        super(quest);
    }

    @Shadow
    public String command;

    @Shadow
    public boolean playerCommand;

    @Inject(method = "claim", at = @At("HEAD"), cancellable = true)
    public void claim(ServerPlayer player, boolean notify, CallbackInfo ci) {
        if (playerCommand) {
            return;
        }

        for (String blacklistCommand : ExtraQuests.getInstance().getConfig().getBlacklistConsole()) {
            if (command.contains(blacklistCommand)) {
                ci.cancel();
            }
        }
    }
}
