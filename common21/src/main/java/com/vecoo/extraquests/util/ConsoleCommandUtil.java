package com.vecoo.extraquests.util;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import lombok.val;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

public class ConsoleCommandUtil {
    public static boolean isBlocked(@NotNull CommandSourceStack source, @NotNull String command, @NotNull Set<String> blacklist) {
        val dispatcher = source.getServer().getCommands().getDispatcher();
        val input = command.trim();
        val result = dispatcher.parse(input.startsWith("/") ? input.substring(1) : input, source.getServer().createCommandSourceStack());

        for (CommandContextBuilder<CommandSourceStack> context = result.getContext(); context != null; context = context.getChild()) {
            if (context.getRootNode() != dispatcher.getRoot() || context.getNodes().isEmpty()) {
                continue;
            }

            CommandNode<CommandSourceStack> node = context.getNodes().get(0).getNode();
            Set<CommandNode<CommandSourceStack>> visited = Collections.newSetFromMap(new IdentityHashMap<>());

            while (node != null && visited.add(node)) {
                for (String blocked : blacklist) {
                    if (blocked != null && matches(node.getName(), blocked)) {
                        return true;
                    }
                }

                node = node.getRedirect();
            }
        }

        return false;
    }

    private static boolean matches(@NotNull String command, @NotNull String blocked) {
        val name = command.toLowerCase(Locale.ROOT);
        String entry = blocked.trim().toLowerCase(Locale.ROOT);

        if (entry.startsWith("/")) {
            entry = entry.substring(1);
        }

        return entry.equals(name) || !entry.contains(":") && entry.equals(name.substring(name.indexOf(':') + 1));
    }
}
