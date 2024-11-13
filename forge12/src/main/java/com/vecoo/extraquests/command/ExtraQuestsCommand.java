package com.vecoo.extraquests.command;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.vecoo.extralib.ExtraLib;
import com.vecoo.extralib.chat.UtilChat;
import com.vecoo.extralib.permission.UtilPermissions;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.task.KeyValueTask;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ExtraQuestsCommand extends CommandBase {
    @Override
    public String getName() {
        return "extraquests";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("equests");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return ExtraQuests.getInstance().getLocale().getExtraQuestsCommand();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return UtilPermissions.hasPermission(sender, "minecraft.command.extraquests", ExtraQuests.getInstance().getPermission().getPermissionCommand());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "add", "reload");
        }

        if (args.length == 2 && args[0].equals("add")) {
            return getListOfStringsMatchingLastWord(args, ExtraLib.getInstance().getServer().getPlayerList().getOnlinePlayerNames());
        }

        if (args.length == 4 && args[0].equals("add")) {
            return getListOfStringsMatchingLastWord(args, "10", "50", "100");
        }

        return Collections.singletonList("");
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        try {
            switch (args[0]) {
                case "add": {
                    executeAdd(sender, ExtraLib.getInstance().getServer().getPlayerList().getPlayerByUsername(args[1]), args[2], Integer.parseInt(args[3]));
                    break;
                }

                case "reload": {
                    executeReload(sender);
                    break;
                }
            }
        } catch (Exception e) {
            sender.sendMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getExtraQuestsCommand()));
        }
    }

    private void executeAdd(ICommandSender source, EntityPlayerMP target, String key, int amount) {
        if (!UtilPermissions.hasPermission(source, "minecraft.command.extraquests", ExtraQuests.getInstance().getPermission().getPermissionCommand())) {
            source.sendMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getNotPermission()));
            return;
        }

        List<KeyValueTask> keyValueTasks = null;

        if (keyValueTasks == null) {
            keyValueTasks = ServerQuestFile.INSTANCE.collect(KeyValueTask.class);
        }

        if (keyValueTasks.isEmpty()) {
            return;
        }
        QuestData data = ServerQuestFile.INSTANCE.getData(target);

        if (data == null) {
            return;
        }

        for (KeyValueTask task : keyValueTasks) {
            TaskData taskData = data.getTaskData(task);
            if (taskData.progress < task.getMaxProgress() && !taskData.isComplete()) {
                ((KeyValueTask.Data) taskData).prgoress(key, amount);
            }
        }

        source.sendMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getAddKeyValueSource()
                .replace("%player%", target.getGameProfile().getName())
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))));

        target.sendMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getAddKeyValueTarget()
                .replace("%key%", key)
                .replace("%value%", String.valueOf(amount))));
    }

    private void executeReload(ICommandSender source) {
        if (!UtilPermissions.hasPermission(source, "minecraft.command.extraquests", ExtraQuests.getInstance().getPermission().getPermissionCommand())) {
            source.sendMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getNotPermission()));
            return;
        }

        ExtraQuests.getInstance().loadConfig();
        ExtraQuests.getInstance().loadStorage();

        source.sendMessage(UtilChat.formatMessage(ExtraQuests.getInstance().getLocale().getReload()));
    }
}