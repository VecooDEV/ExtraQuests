package com.vecoo.extraquests.util;

import de.marhali.json5.Json5Element;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import org.jetbrains.annotations.NotNull;

public class QuestJsonOps extends Json5Ops {
    public static final QuestJsonOps INSTANCE = new QuestJsonOps();

    private QuestJsonOps() {
        super(false);
    }

    @Override
    @NotNull
    public Json5Element createNumeric(@NotNull Number value) {
        if (value instanceof Byte || value instanceof Short) {
            return super.createNumeric(value.intValue());
        }

        return super.createNumeric(value);
    }
}
