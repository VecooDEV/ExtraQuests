package com.vecoo.extraquests.network;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class QuestClientState {
    private static CompoundTag DATA = new CompoundTag();

    public static void update(@NotNull CompoundTag state) {
        DATA = state;
    }

    @NotNull
    public static CompoundTag task(long id) {
        return DATA.getCompound("tasks").getCompound(Long.toUnsignedString(id, 16));
    }

    @NotNull
    public static CompoundTag quest(long id) {
        return DATA.getCompound("quests").getCompound(Long.toUnsignedString(id, 16));
    }

    @NotNull
    public static CompoundTag global(long id) {
        return DATA.getCompound("global").getCompound(Long.toUnsignedString(id, 16));
    }
}
