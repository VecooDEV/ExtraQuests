package com.vecoo.extraquests.task;

import dev.ftb.mods.ftbquests.quest.TeamData;
import org.jetbrains.annotations.NotNull;

public interface TaskSettingsHolder {
    @NotNull
    TaskSettings extraQuests$getSettings();

    boolean extraQuests$canAdvance(@NotNull TeamData data);
}
