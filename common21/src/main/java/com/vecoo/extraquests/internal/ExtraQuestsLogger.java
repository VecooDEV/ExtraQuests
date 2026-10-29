package com.vecoo.extraquests.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtraQuestsLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("com.vecoo.extraquests.ExtraQuests");

    @NotNull
    public static Logger getLogger() {
        return LOGGER;
    }
}
