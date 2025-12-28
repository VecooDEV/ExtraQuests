package com.vecoo.extraquests.command;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vecoo.extralib.chat.UtilChat;
import com.vecoo.extralib.permission.UtilPermission;
import com.vecoo.extralib.player.UtilPlayer;
import com.vecoo.extralib.server.UtilCommand;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import lombok.val;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

public class ExtraQuestsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection commandSelection) {
        dispatcher.register(Commands.literal("equests")
                .requires(p -> UtilPermission.hasPermission(p, "minecraft.command.equests"))
                .then(Commands.literal("key_value")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .suggests(UtilCommand.suggestOnlinePlayers())
                                        .then(Commands.argument("key", StringArgumentType.string())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .suggests(UtilCommand.suggestAmount(Sets.newHashSet(10, 50, 100)))
                                                        .then(Commands.argument("ignore", BoolArgumentType.bool())
                                                                .executes(e -> executeKeyValueAdd(e.getSource(), StringArgumentType.getString(e, "player"),
                                                                        StringArgumentType.getString(e, "key"), IntegerArgumentType.getInteger(e, "amount"), BoolArgumentType.getBool(e, "ignore")))))))))

                .then(Commands.literal("reload")
                        .executes(e -> executeReload(e.getSource()))));
    }

    private static int executeKeyValueAdd(@NotNull CommandSourceStack source, @NotNull String target,
                                          @NotNull String key, int amount, boolean ignore) {
        val localeConfig = ExtraQuests.getInstance().getLocaleConfig();
        val targetUUID = UtilPlayer.findUUID(target);

        if (targetUUID == null) {
            source.sendSystemMessage(UtilChat.formatMessage(localeConfig.getPlayerNotFound()
                    .replace("%player%", target)));
            return 0;
        }

        val file = ServerQuestFile.INSTANCE;
        val teamData = FTBTeamsAPI.api().getManager().getTeamForPlayerID(targetUUID).map(file::getOrCreateTeamData)
                .orElse(file.getOrCreateTeamData(targetUUID));

        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(teamData, key, amount, ignore);
        }

        source.sendSystemMessage(UtilChat.formatMessage(localeConfig.getAddKeyValue()
                .replace("%player%", target)
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))));
        return 1;
    }

    private static int executeReload(@NotNull CommandSourceStack source) {
        val localeConfig = ExtraQuests.getInstance().getLocaleConfig();

        try {
            ExtraQuests.getInstance().loadConfig();
        } catch (Exception e) {
            source.sendSystemMessage(UtilChat.formatMessage(localeConfig.getErrorReload()));
            ExtraQuests.getLogger().error(e.getMessage());
            return 0;
        }

        source.sendSystemMessage(UtilChat.formatMessage(localeConfig.getReload()));
        return 1;
    }
}