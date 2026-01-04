package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import lombok.val;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandReward.class, remap = false)
public abstract class CommandRewardMixin {
    @Unique
    private boolean extraQuests$console;

    @Inject(
            method = "writeData",
            at = @At("TAIL")
    )
    public void writeData(CompoundTag nbt, CallbackInfo ci) {
        if (this.extraQuests$console) {
            nbt.putBoolean("console", true);
        }
    }

    @Inject(
            method = "readData",
            at = @At("TAIL")
    )
    public void readData(CompoundTag nbt, CallbackInfo ci) {
        this.extraQuests$console = nbt.getBoolean("console");
    }

    @Inject(
            method = "writeNetData",
            at = @At("TAIL")
    )
    public void writeNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(this.extraQuests$console);
    }

    @Inject(
            method = "readNetData",
            at = @At("TAIL")
    )
    public void readNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        this.extraQuests$console = buffer.readBoolean();
    }

    @Inject(
            method = "fillConfigGroup",
            at = @At("TAIL")
    )
    public void fillConfigGroup(ConfigGroup config, CallbackInfo ci) {
        config.addBool("console", this.extraQuests$console, v -> this.extraQuests$console = v, false).setNameKey("extraquests.reward.command.console");
    }

    @Redirect(
            method = "claim",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I"
            ),
            remap = true
    )
    public int claim(Commands instance, CommandSourceStack source, String command) {
        if (this.extraQuests$console) {
            val serverConfig = ExtraQuests.getInstance().getServerConfig();

            if (serverConfig.isBlacklistConsole()) {
                for (String blacklistCommand : serverConfig.getBlacklistConsoleList()) {
                    if (command.contains(blacklistCommand)) {
                        return 0;
                    }
                }
            }

            instance.performPrefixedCommand(source.getServer().createCommandSourceStack(), command);
        } else {
            instance.performPrefixedCommand(source, command);
        }
        return 1;
    }
}