package com.vecoo.extraquests.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.vecoo.extraquests.integration.ExtraIntegration;
import com.vecoo.extraquests.task.KeyValueTask;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class KeyValueReward extends Reward {
    private String key = "key";
    private long value = 5L;

    public KeyValueReward(Quest quest) {
        super(quest);
    }

    @Override
    public RewardType getType() {
        return ExtraIntegration.KEY_VALUE_REWARD;
    }

    @Override
    public void writeData(NBTTagCompound nbt) {
        super.writeData(nbt);
        nbt.setString("key", key);
        nbt.setLong("value", value);
    }

    @Override
    public void readData(NBTTagCompound nbt) {
        super.readData(nbt);
        key = nbt.getString("key");
        value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(DataOut buffer) {
        super.writeNetData(buffer);
        buffer.writeString(key);
        buffer.writeVarLong(value);
    }

    @Override
    public void readNetData(DataIn buffer) {
        super.readNetData(buffer);
        key = buffer.readString();
        value = buffer.readVarLong();
    }

    public String getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("key", () -> key, v -> key = v, "").setDisplayName(new TextComponentTranslation("extraquests.key_value.key"));
        config.addLong("value", () -> value, v -> value = v, 100L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("extraquests.key_value.value"));
    }

    private static List<KeyValueTask> keyValueTasks = null;

    @Override
    public void claim(EntityPlayerMP player, boolean notify) {
        if (keyValueTasks == null) {
            keyValueTasks = ServerQuestFile.INSTANCE.collect(KeyValueTask.class);
        }

        if (keyValueTasks.isEmpty()) {
            return;
        }
        QuestData data = ServerQuestFile.INSTANCE.getData(player);

        if (data == null) {
            return;
        }

        for (KeyValueTask task : keyValueTasks) {
            TaskData taskData = data.getTaskData(task);
            if (taskData.progress < task.getMaxProgress() && !taskData.isComplete()) {
                ((KeyValueTask.Data) taskData).prgoress(key, value);
            }
        }
    }

    @Override
    public String getAltTitle() {
        return I18n.format("extraquests.key_value.title", getKey(), getValue());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getButtonText() {
        return "+" + value;
    }
}