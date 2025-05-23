package com.vecoo.extraquests.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vecoo.extralib.chat.UtilChat;
import com.vecoo.extralib.permission.UtilPermission;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

public class ExtraQuestsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection commandSelection) {
        dispatcher.register(Commands.literal("equests")
                .requires(p -> UtilPermission.hasPermission(p, "minecraft.command.equests"))
                .then(Commands.literal("key_value")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .suggests((s, builder) -> {
                                            for (String playerName : s.getSource().getOnlinePlayerNames()) {
                                                if (playerName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                                                    builder.suggest(playerName);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("key", StringArgumentType.string())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .suggests((s, builder) -> {
                                                            for (int amount : Arrays.asList(10, 50, 100)) {
                                                                builder.suggest(amount);
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(e -> executeKeyValueAdd(e.getSource(), EntityArgument.getPlayer(e, "player"), StringArgumentType.getString(e, "key"), IntegerArgumentType.getInteger(e, "amount"))))))))
                .then(Commands.literal("reload")
                        .executes(e -> executeReload(e.getSource()))));
    }

    private static int executeKeyValueAdd(CommandSourceStack source, ServerPlayer target, String key, int amount) {
        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getData(target), key, amount);
        }

        source.sendSuccess(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getAddKeyValueSource()
                .replace("%player%", target.getGameProfile().getName())
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))), false);

        target.sendSystemMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getAddKeyValueTarget()
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))));
        return 1;
    }

    private static int executeReload(CommandSourceStack source) {
        ExtraQuests.getInstance().loadConfig();
        ExtraQuests.getInstance().loadStorage();

        source.sendSuccess(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getReload()), false);
        return 1;
    }
}