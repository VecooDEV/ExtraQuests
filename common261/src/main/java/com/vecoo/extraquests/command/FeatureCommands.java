package com.vecoo.extraquests.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import com.vecoo.extraquests.runtime.TaskProgress;
import com.vecoo.extraquests.task.TaskSettings;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import lombok.val;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class FeatureCommands {
    private static final Logger LOGGER = ExtraQuestsLogger.getLogger();

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String name : new String[]{"extraquests", "equests"}) {
            dispatcher.register(Commands.literal(name)
                    .then(Commands.literal("shared")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.literal("reset")
                                    .then(Commands.argument("task", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                ServerQuestFile.getInstance().collect(Task.class).stream().filter(task -> TaskSettings.get(task).isShared()).forEach(task -> builder.suggest(Long.toUnsignedString(task.id, 16)));

                                                return builder.buildFuture();
                                            })
                                            .executes(context -> reset(context.getSource(), StringArgumentType.getString(context, "task")))))));
        }
    }

    private static int reset(@NotNull CommandSourceStack source, @NotNull String id) {
        val file = ServerQuestFile.exists() ? ServerQuestFile.getInstance() : null;
        Task task;

        try {
            task = file.getTask(Long.parseUnsignedLong(id, 16));
            if (task != null && !TaskSettings.get(task).isShared()) {
                task = null;
            }
        } catch (NumberFormatException e) {
            task = null;
        }

        if (task == null) {
            source.sendSystemMessage(Component.translatable("extraquests.shared.invalid_task", id));

            return 0;
        }

        for (val data : file.getAllTeamData()) {
            if (data.isLocked() && data.isStarted(task.getQuest())) {
                source.sendSystemMessage(Component.translatable("extraquests.shared.locked_team"));
                return 0;
            }
        }

        TaskProgress.resetShared(task, source.getServer());
        source.sendSystemMessage(Component.translatable("extraquests.shared.reset", id));
        return 1;
    }
}
