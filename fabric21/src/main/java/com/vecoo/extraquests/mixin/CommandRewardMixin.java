package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.config.ServerConfig;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandReward.class, remap = false)
public abstract class CommandRewardMixin {
    @Unique
    private boolean console;

    @Inject(method = "writeData", at = @At("TAIL"))
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (this.console) {
            nbt.putBoolean("console", true);
        }
    }

    @Inject(method = "readData", at = @At("TAIL"))
    public void readData(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        this.console = nbt.getBoolean("console");
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    public void writeNetData(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(this.console);
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    public void readNetData(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        this.console = buffer.readBoolean();
    }

    @Inject(method = "fillConfigGroup", at = @At("TAIL"))
    public void fillConfigGroup(ConfigGroup config, CallbackInfo ci) {
        config.addBool("console", this.console, v -> this.console = v, false).setNameKey("extraquests.reward.command.console");
    }

    @Redirect(method = "claim", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"), remap = true)
    public void claim(Commands instance, CommandSourceStack source, String command) {
        if (this.console) {
            ServerConfig config = ExtraQuests.getInstance().getConfig();

            if (config.isBlacklistConsole()) {
                for (String blacklistCommand : config.getBlacklistConsoleList()) {
                    if (command.contains(blacklistCommand)) {
                        return;
                    }
                }
            }

            instance.performPrefixedCommand(source.getServer().createCommandSourceStack(), command);
        } else {
            instance.performPrefixedCommand(source, command);
        }
    }
}
