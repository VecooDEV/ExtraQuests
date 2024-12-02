package com.vecoo.extraquests.util;

import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

public class PermissionNodes {
    public static PermissionNode<Boolean> EXTRAQUESTS_COMMAND = new PermissionNode<>(
            "minecraft",
            "command.equests",
            PermissionTypes.BOOLEAN,
            (p, uuid, permissionDynamicContexts) -> false);
}