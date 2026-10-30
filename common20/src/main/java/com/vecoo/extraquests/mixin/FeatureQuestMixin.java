package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.quest.QuestSettings;
import com.vecoo.extraquests.quest.QuestSettingsHolder;
import com.vecoo.extraquests.runtime.QuestFeatures;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import lombok.val;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = Quest.class, remap = false)
public abstract class FeatureQuestMixin implements QuestSettingsHolder {
    @Unique
    private final QuestSettings extraQuests$settings = new QuestSettings();

    @Override
    @NotNull
    public QuestSettings extraQuests$getSettings() {
        return this.extraQuests$settings;
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void extraQuests$writeCalendar(@NotNull CompoundTag tag, @NotNull CallbackInfo ci) {
        tag.put("extraquests_calendar", this.extraQuests$settings.write());
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void extraQuests$readCalendar(@NotNull CompoundTag tag, @NotNull CallbackInfo ci) {
        this.extraQuests$settings.read(tag.getCompound("extraquests_calendar"));
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void extraQuests$writeNetCalendar(@NotNull FriendlyByteBuf buffer, @NotNull CallbackInfo ci) {
        buffer.writeNbt(this.extraQuests$settings.write());
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void extraQuests$readNetCalendar(@NotNull FriendlyByteBuf buffer, @NotNull CallbackInfo ci) {
        val tag = buffer.readNbt();

        this.extraQuests$settings.read(tag == null ? new CompoundTag() : tag);
    }

    @Inject(method = "checkRepeatable", at = @At("HEAD"), cancellable = true)
    private void extraQuests$scheduledRepeat(@NotNull TeamData data, @NotNull UUID player, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (QuestFeatures.usesScheduledReset((Quest) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onCompleted", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/TeamData;checkAutoCompletion(Ldev/ftb/mods/ftbquests/quest/Quest;)V"))
    private void extraQuests$calendarStreak(@NotNull QuestProgressEventData<?> data, @NotNull CallbackInfo ci) {
        QuestFeatures.onCompleted((Quest) (Object) this, data.getTeamData());
    }
}
