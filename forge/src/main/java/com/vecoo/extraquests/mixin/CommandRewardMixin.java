package com.vecoo.extraquests.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.CommandReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(CommandReward.class)
public abstract class CommandRewardMixin extends Reward {
    @Shadow(remap = false)
    private String command = "";
    private String command_two = "";
    private String command_three = "";
    @Shadow(remap = false)
    private boolean elevatePerms = false;
    @Shadow(remap = false)
    private boolean silent = false;
    private boolean console = false;

    public CommandRewardMixin(long id, Quest q) {
        super(id, q);
    }

    /**
     * @author Vecoo
     * @reason Add new nbt
     */
    @Overwrite(remap = false)
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("command", command);
        nbt.putString("command_two", command_two);
        nbt.putString("command_three", command_three);
        nbt.putBoolean("elevate_perms", elevatePerms);
        nbt.putBoolean("silent", silent);
        nbt.putBoolean("console", console);
    }

    /**
     * @author Vecoo
     * @reason Add new nbt
     */
    @Overwrite(remap = false)
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        command = nbt.getString("command");
        command_two = nbt.getString("command_two");
        command_three = nbt.getString("command_three");
        elevatePerms = nbt.getBoolean("elevate_perms");
        silent = nbt.getBoolean("silent");
        console = nbt.getBoolean("console");
    }

    /**
     * @author Vecoo
     * @reason Add new nbt
     */
    @Overwrite(remap = false)
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(command, Short.MAX_VALUE);
        buffer.writeUtf(command_two, Short.MAX_VALUE);
        buffer.writeUtf(command_three, Short.MAX_VALUE);
        buffer.writeBoolean(elevatePerms);
        buffer.writeBoolean(silent);
        buffer.writeBoolean(console);
    }

    /**
     * @author Vecoo
     * @reason Add new nbt
     */
    @Overwrite(remap = false)
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        command = buffer.readUtf(Short.MAX_VALUE);
        command_two = buffer.readUtf(Short.MAX_VALUE);
        command_three = buffer.readUtf(Short.MAX_VALUE);
        elevatePerms = buffer.readBoolean();
        silent = buffer.readBoolean();
        console = buffer.readBoolean();
    }

    /**
     * @author Vecoo
     * @reason Add new nbt
     */
    @Overwrite(remap = false)
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("command", command, v -> command = v, "/say Hi, @team!").setNameKey("extraquests.reward.mixins.command");
        config.addString("command_two", command_two, v -> command_two = v, "").setNameKey("extraquests.reward.mixins.command_two");
        config.addString("command_three", command_three, v -> command_three = v, "").setNameKey("extraquests.reward.mixins.command_three");
        config.addBool("elevate", elevatePerms, v -> elevatePerms = v, false);
        config.addBool("silent", silent, v -> silent = v, false);
        config.addBool("console", console, v -> console = v, false).setNameKey("extraquests.reward.mixins.console");
    }

    /**
     * @author Vecoo
     * @reason Add new options
     */
    @Overwrite(remap = false)
    public void claim(ServerPlayer player, boolean notify) {
        Map<String, Object> overrides = new HashMap<>();

        BlockPos pos = player.blockPosition();
        overrides.put("x", pos.getX());
        overrides.put("y", pos.getY());
        overrides.put("z", pos.getZ());

        if (getQuestChapter() != null) {
            overrides.put("chapter", getQuestChapter());
        }

        overrides.put("quest", quest);

        String cmd = command;
        String cmdTwo = command_two;
        String cmdThree = command_three;
        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            if (entry.getValue() != null) {
                cmd = cmd.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                if (!cmdTwo.isEmpty()) {
                    cmdTwo = cmdTwo.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
                if (!cmdThree.isEmpty()) {
                    cmdThree = cmdThree.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
            }
        }

        CommandSourceStack source = player.createCommandSourceStack();
        if (elevatePerms) {
            source = source.withPermission(2);
        }
        if (silent) {
            source = source.withSuppressedOutput();
        }
        if (console) {
            String cmdConsole = cmd.replace("@p", player.getName().getString());
            player.server.getCommands().performPrefixedCommand(source.getServer().createCommandSourceStack(), cmdConsole);
            if (!cmdTwo.isEmpty()) {
                String cmdTwoConsole = cmd.replace("@p", player.getName().getString());
                player.server.getCommands().performPrefixedCommand(source.getServer().createCommandSourceStack(), cmdTwoConsole);
            }
            if (!cmdThree.isEmpty()) {
                String cmdThreeConsole = cmd.replace("@p", player.getName().getString());
                player.server.getCommands().performPrefixedCommand(source.getServer().createCommandSourceStack(), cmdThreeConsole);
            }
        } else {
            player.server.getCommands().performPrefixedCommand(source, cmd);
            if (!cmdTwo.isEmpty()) {
                player.server.getCommands().performPrefixedCommand(source, cmdTwo);
            }
            if (!cmdThree.isEmpty()) {
                player.server.getCommands().performPrefixedCommand(source, cmdThree);
            }
        }
    }
}