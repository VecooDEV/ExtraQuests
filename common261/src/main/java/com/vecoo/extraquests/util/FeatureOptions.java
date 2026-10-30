package com.vecoo.extraquests.util;

import dev.ftb.mods.ftblibrary.util.NameMap;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

public class FeatureOptions {
    public static final NameMap<Integer> DAYS = NameMap.of(1, List.of(1, 2, 3, 4, 5, 6, 7))
            .id(value -> DayOfWeek.of(value).name().toLowerCase(Locale.ROOT))
            .baseNameKey("extraquests.weekday").create();
    public static final NameMap<Integer> OPERATIONS = NameMap.of(0, List.of(0, 1, 2))
            .id(value -> switch (value) {
                case 1 -> "base";
                case 2 -> "total";
                default -> "value";
            }).baseNameKey("extraquests.operation").create();
    public static final NameMap<Integer> STACKING = NameMap.of(0, List.of(0, 1, 2))
            .id(value -> switch (value) {
                case 1 -> "add";
                case 2 -> "strongest";
                default -> "replace";
            }).baseNameKey("extraquests.stacking").create();
}
