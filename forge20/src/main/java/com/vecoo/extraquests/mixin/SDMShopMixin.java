package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.config.ServerConfig;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.sixik.sdmshop.shop.entry_types.CommandEntryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = CommandEntryType.class, remap = false)
public abstract class SDMShopMixin {
    @Unique
    private boolean console;

    @Inject(
            method = "serialize()Lnet/minecraft/nbt/CompoundTag;",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void serialize(CallbackInfoReturnable<CompoundTag> cir, CompoundTag nbt) {
        if (this.console) {
            nbt.putBoolean("console", true);
        }
    }

    @Inject(
            method = "deserialize(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL")
    )
    public void deserialize(CompoundTag nbt, CallbackInfo ci) {
        this.console = nbt.getBoolean("console");
    }

    @Inject(
            method = "getConfig",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;addInt(Ljava/lang/String;ILjava/util/function/Consumer;III)Ldev/ftb/mods/ftblibrary/config/IntConfig;"
            )
    )
    public void fillConfigGroup(ConfigGroup config, CallbackInfo ci) {
        config.addBool("console", this.console, v -> this.console = v, false).setNameKey("extraquests.reward.command.console");
    }

    @Redirect(
            method = "onBuy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I"
            ),
            remap = true
    )
    public int onBuy(Commands instance, CommandSourceStack source, String command) {
        if (this.console) {
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
