package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.runtime.QuestFeatures;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public abstract class PlayerDamageMixin {
    @ModifyArg(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V"), index = 0)
    private float extraQuests$damage(float health) {
        if ((Object) this instanceof ServerPlayer player && health < player.getHealth()) {
            QuestFeatures.failChallenges(player, "damage");
        }

        return health;
    }
}
