package com.vecoo.extraquests.service;

import com.vecoo.extralib.shade.spongepowered.configurate.objectmapping.ConfigSerializable;
import com.vecoo.extralib.shade.spongepowered.configurate.objectmapping.meta.Setting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@ToString
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ConfigSerializable
public class PlayerStorage {
    @Nonnull
    @Setting("playerUUID")
    private final UUID playerUUID;
    @Nonnull
    @Setting("questTimers")
    private final Map<String, Long> questTimers;

    @Nonnull
    private transient final AtomicBoolean dirty = new AtomicBoolean(true);

    public void addQuestTimer(@Nonnull String questID, long endTime) {
        this.questTimers.put(questID, endTime);
    }

    public void removeQuestTimer(@Nonnull String questID) {
        this.questTimers.remove(questID);
    }

    @Nonnull
    public PlayerStorage copy() {
        return new PlayerStorage(this.playerUUID, new HashMap<>(this.questTimers));
    }
}