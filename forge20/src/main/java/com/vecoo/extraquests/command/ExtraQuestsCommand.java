package com.vecoo.extraquests.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vecoo.extralib.chat.UtilChat;
import com.vecoo.extralib.permission.UtilPermission;
import com.vecoo.extralib.player.UtilPlayer;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.util.PermissionNodes;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class ExtraQuestsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("equests")
                .requires(s -> UtilPermission.hasPermission(s, PermissionNodes.EXTRAQUESTS_COMMAND))
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
                                                            for (int amount : List.of(10, 50, 100)) {
                                                                builder.suggest(amount);
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(e -> executeKeyValueAdd(e.getSource(), StringArgumentType.getString(e, "player"), StringArgumentType.getString(e, "key"), IntegerArgumentType.getInteger(e, "amount"))))))))
                .then(Commands.literal("reload")
                        .executes(e -> executeReload(e.getSource()))));
    }

    private static int executeKeyValueAdd(CommandSourceStack source, String target, String key, int amount) {
        UUID targetUUID = UtilPlayer.getUUID(target);

        if (targetUUID == null) {
            source.sendSystemMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getPlayerNotFound()
                    .replace("%player%", target)));
            return 0;
        }

        ServerQuestFile file = ServerQuestFile.INSTANCE;
        TeamData teamData = FTBTeamsAPI.api().getManager().getTeamForPlayerID(targetUUID).map(file::getOrCreateTeamData).orElse(file.getOrCreateTeamData(targetUUID));

        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(teamData, key, amount);
        }

        source.sendSystemMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getAddKeyValue()
                .replace("%player%", target)
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))));
        return 1;
    }

    private static int executeReload(CommandSourceStack source) {
        ExtraQuests.getInstance().loadConfig();

        source.sendSystemMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getReload()));
        return 1;
    }
}