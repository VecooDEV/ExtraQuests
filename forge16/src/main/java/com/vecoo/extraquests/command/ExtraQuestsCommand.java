package com.vecoo.extraquests.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vecoo.extralib.chat.UtilChat;
import com.vecoo.extralib.permission.UtilPermission;
import com.vecoo.extralib.player.UtilPlayer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.util.Arrays;
import java.util.UUID;

public class ExtraQuestsCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("equests")
                .requires(p -> UtilPermission.hasPermission(p, "minecraft.command.equests"))
                .then(Commands.literal("key_value")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", StringArgumentType.string())
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
                                                        .executes(e -> executeKeyValueAdd(e.getSource(), StringArgumentType.getString(e, "player"), StringArgumentType.getString(e, "key"), IntegerArgumentType.getInteger(e, "amount"))))))))
                .then(Commands.literal("reload")
                        .executes(e -> executeReload(e.getSource()))));
    }

    private static int executeKeyValueAdd(CommandSource source, String target, String key, int amount) {
        UUID targetUUID = UtilPlayer.getUUID(target);

        if (targetUUID == null) {
            source.sendSuccess(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getPlayerNotFound()
                    .replace("%player%", target)), false);
            return 0;
        }

        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getData(FTBTeamsAPI.getPlayerTeamID(targetUUID)), key, amount);
        }

        source.sendSuccess(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getAddKeyValue()
                .replace("%player%", target)
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))), false);
        return 1;
    }

    private static int executeReload(CommandSource source) {
        ExtraQuests.getInstance().loadConfig();

        source.sendSuccess(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getReload()), false);
        return 1;
    }
}