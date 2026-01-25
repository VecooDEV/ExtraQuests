package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import lombok.val;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandReward.class, remap = false)
public abstract class CommandRewardMixin {
    @Shadow
    private String command;
    @Unique
    private boolean extraQuests$console;

    @Inject(
            method = "writeData",
            at = @At("TAIL")
    )
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (this.extraQuests$console) {
            nbt.putBoolean("console", true);
        }
    }

    @Inject(
            method = "readData",
            at = @At("TAIL")
    )
    public void readData(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        this.extraQuests$console = nbt.getBoolean("console");
    }

    @Inject(
            method = "writeNetData",
            at = @At("TAIL")
    )
    public void writeNetData(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(this.extraQuests$console);
    }

    @Inject(
            method = "readNetData",
            at = @At("TAIL")
    )
    public void readNetData(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
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
                    target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"
            ),
            remap = true
    )
    public void claim(Commands instance, CommandSourceStack source, String command) {
        if (this.extraQuests$console) {
            val serverConfig = ExtraQuests.getInstance().getServerConfig();

            if (serverConfig.isBlacklistConsole()) {
                for (String blacklistCommand : serverConfig.getBlacklistConsoleList()) {
                    if (command.contains(blacklistCommand)) {
                        return;
                    }
                }
            }

            if (source.getPlayer() != null) {
                command = command.replace("@p", source.getPlayer().getName().getString());
            }

            instance.performPrefixedCommand(source.getServer().createCommandSourceStack(), command);
        } else {
            instance.performPrefixedCommand(source, command);
        }
    }
}