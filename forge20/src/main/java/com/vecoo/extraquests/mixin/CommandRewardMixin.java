package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.config.ServerConfig;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
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
    private boolean console;

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

    @Redirect(method = "claim", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I"), remap = true)
    public int claim(Commands instance, CommandSourceStack source, String command) {
        if (console) {
            ServerConfig config = ExtraQuests.getInstance().getConfig();

            if (config.isBlacklistConsole()) {
                for (String blacklistCommand : config.getBlacklistConsoleList()) {
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