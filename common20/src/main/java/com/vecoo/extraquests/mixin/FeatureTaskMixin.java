package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.runtime.TaskProgress;
import com.vecoo.extraquests.task.TaskSettings;
import com.vecoo.extraquests.task.TaskSettingsHolder;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import lombok.val;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Task.class, remap = false)
public abstract class FeatureTaskMixin implements TaskSettingsHolder {
    @Unique
    private final TaskSettings extraQuests$settings = new TaskSettings();

    @Shadow
    protected abstract boolean checkTaskSequence(@NotNull TeamData data);

    @Override
    @NotNull
    public TaskSettings extraQuests$getSettings() {
        return this.extraQuests$settings;
    }

    @Override
    public boolean extraQuests$canAdvance(@NotNull TeamData data) {
        if (!data.isLocked() && checkTaskSequence(data)) {
            return data.canStartTasks(((Task) (Object) this).getQuest());
        }

        return false;
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void extraQuests$writeOptions(@NotNull CompoundTag tag, @NotNull CallbackInfo ci) {
        tag.put("extraquests_options", this.extraQuests$settings.write());
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void extraQuests$readOptions(@NotNull CompoundTag tag, @NotNull CallbackInfo ci) {
        this.extraQuests$settings.read(tag.getCompound("extraquests_options"));
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void extraQuests$writeNetOptions(@NotNull FriendlyByteBuf buffer, @NotNull CallbackInfo ci) {
        buffer.writeNbt(this.extraQuests$settings.write());
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void extraQuests$readNetOptions(@NotNull FriendlyByteBuf buffer, @NotNull CallbackInfo ci) {
        val tag = buffer.readNbt();

        this.extraQuests$settings.read(tag == null ? new CompoundTag() : tag);
    }

    @Inject(method = "forceProgress", at = @At("HEAD"))
    private void extraQuests$resetState(@NotNull TeamData data, @NotNull ProgressChange change, @NotNull CallbackInfo ci) {
        if (change.shouldReset()) {
            TaskProgress.onReset((Task) (Object) this, data);
        }
    }
}
