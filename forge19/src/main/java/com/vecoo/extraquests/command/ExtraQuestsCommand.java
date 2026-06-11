package com.vecoo.extraquests.command;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vecoo.extralib.util.CommandUtil;
import com.vecoo.extralib.util.PermissionUtil;
import com.vecoo.extralib.util.PlayerUtil;
import com.vecoo.extralib.util.TextUtil;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.task.KeyValueTask;
import com.vecoo.extraquests.util.PermissionNodes;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import lombok.val;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

public class ExtraQuestsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String command : Sets.newHashSet("extraquests", "equests")) {
            dispatcher.register(Commands.literal(command)
                    .requires(s -> PermissionUtil.hasPermission(s, PermissionNodes.EXTRAQUESTS_COMMAND))
                    .then(Commands.literal("key_value")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("player", StringArgumentType.string())
                                            .suggests(CommandUtil.suggestOnlinePlayers())
                                            .then(Commands.argument("key", StringArgumentType.string())
                                                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                            .suggests(CommandUtil.suggestAmount(Sets.newHashSet(10, 50, 100)))
                                                            .then(Commands.argument("ignore", BoolArgumentType.bool())
                                                                    .executes(e -> executeKeyValueAdd(e.getSource(), StringArgumentType.getString(e, "player"),
                                                                            StringArgumentType.getString(e, "key"), IntegerArgumentType.getInteger(e, "amount"), BoolArgumentType.getBool(e, "ignore")))))))))

                    .then(Commands.literal("reload")
                            .executes(e -> executeReload(e.getSource()))));
        }
    }

    private static int executeKeyValueAdd(@NotNull CommandSourceStack source, @NotNull String target,
                                          @NotNull String key, int amount, boolean ignore) {
        val localeConfig = ExtraQuests.getInstance().getLocaleConfig();
        val targetUUID = PlayerUtil.findUUID(target);

        if (targetUUID == null) {
            source.sendSuccess(TextUtil.formatMessage(localeConfig.getPlayerNotFound()
                    .replace("%player%", target)), false);
            return 0;
        }

        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            task.progress(ServerQuestFile.INSTANCE.getData(FTBTeamsAPI.getPlayerTeamID(targetUUID)), key, amount, ignore);
        }

        source.sendSuccess(TextUtil.formatMessage(localeConfig.getAddKeyValue()
                .replace("%player%", target)
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))), false);
        return 1;
    }

    private static int executeReload(@NotNull CommandSourceStack source) {
        val localeConfig = ExtraQuests.getInstance().getLocaleConfig();

        try {
            ExtraQuests.getInstance().loadConfig();
        } catch (Exception e) {
            source.sendSuccess(TextUtil.formatMessage(localeConfig.getErrorReload()), false);
            ExtraQuests.getLogger().error(e.getMessage());
            return 0;
        }

        source.sendSuccess(TextUtil.formatMessage(localeConfig.getReload()), false);
        return 1;
    }
}