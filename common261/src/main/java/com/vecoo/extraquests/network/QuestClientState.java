package com.vecoo.extraquests.network;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class QuestClientState {
    private static volatile CompoundTag DATA = new CompoundTag();

    public static void update(@NotNull CompoundTag state) {
        DATA = state;
    }

    @NotNull
    public static CompoundTag task(long id) {
        return DATA.getCompoundOrEmpty("tasks").getCompoundOrEmpty(Long.toUnsignedString(id, 16));
    }

    @NotNull
    public static CompoundTag quest(long id) {
        return DATA.getCompoundOrEmpty("quests").getCompoundOrEmpty(Long.toUnsignedString(id, 16));
    }

    @NotNull
    public static CompoundTag global(long id) {
        return DATA.getCompoundOrEmpty("global").getCompoundOrEmpty(Long.toUnsignedString(id, 16));
    }
}
