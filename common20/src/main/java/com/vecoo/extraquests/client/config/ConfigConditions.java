package com.vecoo.extraquests.client.config;

import dev.ftb.mods.ftblibrary.config.ConfigValue;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

public class ConfigConditions {
    private static final Map<ConfigValue<?>, BooleanSupplier> CONDITIONS = new WeakHashMap<>();

    public static void when(@NotNull ConfigValue<?> value, @NotNull BooleanSupplier condition) {
        CONDITIONS.put(value, condition);
    }

    public static boolean isEnabled(@NotNull ConfigValue<?> value) {
        val condition = CONDITIONS.get(value);

        return condition == null || condition.getAsBoolean();
    }
}
