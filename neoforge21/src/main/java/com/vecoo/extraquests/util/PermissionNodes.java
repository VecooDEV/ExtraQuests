package com.vecoo.extraquests.util;

import com.vecoo.extralib.permission.UtilPermission;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PermissionNodes {
    public static final Set<PermissionNode<?>> PERMISSION_LIST = new HashSet<>();

    public static PermissionNode<Boolean> EXTRAQUESTS_COMMAND = UtilPermission.getPermissionNode("minecraft.command.equests");

    public static void registerPermission(@NotNull PermissionGatherEvent.Nodes event) {
        PERMISSION_LIST.add(EXTRAQUESTS_COMMAND);

        for (PermissionNode<?> node : PERMISSION_LIST) {
            if (!event.getNodes().contains(node)) {
                event.addNodes(node);
            }
        }
    }
}