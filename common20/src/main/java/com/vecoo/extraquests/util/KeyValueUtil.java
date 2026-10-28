package com.vecoo.extraquests.util;

import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import lombok.val;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KeyValueUtil {
    public static void addProgress(@Nullable ServerPlayer player, @NotNull TeamData teamData, @NotNull String key, long value, boolean ignore) {
        val file = ServerQuestFile.INSTANCE;
        val currentPlayer = file.getCurrentPlayer();

        if (currentPlayer != null && currentPlayer != player) {
            file.server.tell(new TickTask(file.server.getTickCount(), () -> addProgress(player, teamData, key, value, ignore)));
            return;
        }

        Runnable progress = () -> {
            for (KeyValueTask task : file.collect(KeyValueTask.class)) {
                task.progress(teamData, key, value, ignore);
            }
        };

        if (player != null && currentPlayer == null) {
            file.withPlayerContext(player, progress);
        } else {
            progress.run();
        }
    }
}
