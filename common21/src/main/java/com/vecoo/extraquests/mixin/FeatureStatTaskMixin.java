package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.runtime.TaskProgress;
import com.vecoo.extraquests.task.TaskSettings;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.StatTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import lombok.val;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatTask.class)
public abstract class FeatureStatTaskMixin {
    @Shadow
    private ResourceLocation stat;

    @Inject(method = "submitTask", at = @At("HEAD"), cancellable = true)
    private void extraQuests$statSample(@NotNull TeamData data, @NotNull ServerPlayer player, @NotNull ItemStack stack, @NotNull CallbackInfo ci) {
        val task = (Task) (Object) this;
        val settings = TaskSettings.get(task);

        if (settings.isShared() || settings.timed()) {
            if (this.stat == null) {
                ci.cancel();

                return;
            }

            ResourceLocation id = BuiltInRegistries.CUSTOM_STAT.get(this.stat);

            if (id == null) {
                id = BuiltInRegistries.CUSTOM_STAT.get(ResourceLocation.tryParse(this.stat.getPath()));
            }

            if (id != null) {
                TaskProgress.statSample(task, data, player, player.getStats().getValue(Stats.CUSTOM.get(id)));
            }

            ci.cancel();
        }
    }
}
