package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.util.ConsoleCommandUtil;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import lombok.val;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
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
    public void writeData(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider, @NotNull CallbackInfo ci) {
        if (this.extraQuests$console) {
            compoundTag.putBoolean("console", true);
        }
    }

    @Inject(
            method = "readData",
            at = @At("TAIL")
    )
    public void readData(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider, @NotNull CallbackInfo ci) {
        this.extraQuests$console = compoundTag.getBoolean("console");
    }

    @Inject(
            method = "writeNetData",
            at = @At("TAIL")
    )
    public void writeNetData(@NotNull RegistryFriendlyByteBuf byteBuf, @NotNull CallbackInfo ci) {
        byteBuf.writeBoolean(this.extraQuests$console);
    }

    @Inject(
            method = "readNetData",
            at = @At("TAIL")
    )
    public void readNetData(@NotNull RegistryFriendlyByteBuf byteBuf, @NotNull CallbackInfo ci) {
        this.extraQuests$console = byteBuf.readBoolean();
    }

    @Inject(
            method = "fillConfigGroup",
            at = @At("TAIL")
    )
    public void fillConfigGroup(@NotNull ConfigGroup config, @NotNull CallbackInfo ci) {
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
    public void claim(@NotNull Commands instance, @NotNull CommandSourceStack source, @NotNull String command) {
        if (this.extraQuests$console) {
            val serverConfig = ExtraQuests.getInstance().getServerConfig();

            if (serverConfig.isBlacklistConsole() && ConsoleCommandUtil.isBlocked(source, command, serverConfig.getBlacklistConsoleList())) {
                return;
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
