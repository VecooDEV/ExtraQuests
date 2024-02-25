package com.vecoo.extraquests;
import com.vecoo.extraquests.reward.EconomyReward;
import com.vecoo.extraquests.reward.KeyValueReward;
import com.vecoo.extraquests.task.EconomyTask;
import com.vecoo.extraquests.task.KeyValueTask;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(ExtraQuests.MOD_ID)
public class ExtraQuests {

    public static final String MOD_ID = "extraquests";

    public ExtraQuests() {
        KeyValueTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueTask::new,
                () -> Icon.getIcon("minecraft:item/paper"));

        EconomyTask.TYPE = TaskTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "economy"), EconomyTask::new,
                () -> Icon.getIcon("craftitems:item/money_sets/money_sets_0"));

        KeyValueReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "key_value"), KeyValueReward::new,
                () -> Icon.getIcon("minecraft:item/paper"));

        EconomyReward.TYPE = RewardTypes.register(new ResourceLocation(ExtraQuests.MOD_ID, "economy"), EconomyReward::new,
                () -> Icon.getIcon("craftitems:item/money_sets/money_sets_0"));
    }
}