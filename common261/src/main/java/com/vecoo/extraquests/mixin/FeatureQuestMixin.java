package com.vecoo.extraquests.mixin;

import com.vecoo.extraquests.quest.QuestSettings;
import com.vecoo.extraquests.quest.QuestSettingsHolder;
import com.vecoo.extraquests.runtime.QuestFeatures;
import com.vecoo.extraquests.util.QuestJsonOps;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.api.event.progress.ProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import lombok.val;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Quest.class)
public abstract class FeatureQuestMixin implements QuestSettingsHolder {
    @Unique
    private final QuestSettings extraQuests$settings = new QuestSettings();

    @Override
    @NotNull
    public QuestSettings extraQuests$getSettings() {
        return this.extraQuests$settings;
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void extraQuests$writeCalendar(@NotNull Json5Object tag, @NotNull HolderLookup.Provider provider, @NotNull CallbackInfo ci) {
        tag.add("extraquests_calendar", CompoundTag.CODEC.encodeStart(QuestJsonOps.INSTANCE, this.extraQuests$settings.write()).getOrThrow());
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void extraQuests$readCalendar(@NotNull Json5Object tag, @NotNull HolderLookup.Provider provider, @NotNull CallbackInfo ci) {
        this.extraQuests$settings.read(Json5Util.fetch(tag, "extraquests_calendar", CompoundTag.CODEC).orElseGet(CompoundTag::new));
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void extraQuests$writeNetCalendar(@NotNull RegistryFriendlyByteBuf buffer, @NotNull CallbackInfo ci) {
        buffer.writeNbt(this.extraQuests$settings.write());
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void extraQuests$readNetCalendar(@NotNull RegistryFriendlyByteBuf buffer, @NotNull CallbackInfo ci) {
        val tag = buffer.readNbt();

        this.extraQuests$settings.read(tag == null ? new CompoundTag() : tag);
    }

    @Inject(method = "resetProgressIfRepeatable", at = @At("HEAD"), cancellable = true)
    private void extraQuests$scheduledRepeat(@NotNull TeamData data, @NotNull UUID player, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (QuestFeatures.usesScheduledReset((Quest) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onCompleted", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/TeamData;checkAutoCompletion(Ldev/ftb/mods/ftbquests/quest/Quest;)V"))
    private void extraQuests$calendarStreak(@NotNull ProgressEventData<?> data, @NotNull CallbackInfo ci) {
        QuestFeatures.onCompleted((Quest) (Object) this, data.teamData());
    }
}
