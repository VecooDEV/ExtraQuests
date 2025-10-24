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
import com.vecoo.extraquests.integration.QuestsIntegration;
import com.vecoo.extraquests.task.KeyValueTask;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class KeyValueReward extends Reward {
    private String key;
    private long value;
    private boolean ignore;

    public KeyValueReward(Quest quest) {
        super(quest);
        this.key = "key";
        this.value = 5L;
        this.ignore = false;
    }

    @Override
    @Nonnull
    public RewardType getType() {
        return QuestsIntegration.KEY_VALUE_REWARD;
    }

    @Override
    public void writeData(@Nonnull NBTTagCompound nbt) {
        super.writeData(nbt);
        nbt.setString("key", this.key);
        nbt.setLong("value", this.value);
        nbt.setBoolean("ignore", this.ignore);
    }

    @Override
    public void readData(@Nonnull NBTTagCompound nbt) {
        super.readData(nbt);
        this.key = nbt.getString("key");
        this.value = nbt.getLong("value");
        this.ignore = nbt.getBoolean("ignore");
    }

    @Override
    public void writeNetData(@Nonnull DataOut buffer) {
        super.writeNetData(buffer);
        buffer.writeString(this.key);
        buffer.writeVarLong(this.value);
        buffer.writeBoolean(this.ignore);
    }

    @Override
    public void readNetData(@Nonnull DataIn buffer) {
        super.readNetData(buffer);
        this.key = buffer.readString();
        this.value = buffer.readVarLong();
        this.ignore = buffer.readBoolean();
    }

    public String getKey() {
        return this.key;
    }

    public long getValue() {
        return this.value;
    }

    public boolean isIgnore() {
        return this.ignore;
    }

    @Override
    @Nonnull
    public String getAltTitle() {
        return I18n.format("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @Nonnull
    public String getButtonText() {
        return "+" + this.value;
    }

    @Override
    public void getConfig(@Nonnull ConfigGroup config) {
        super.getConfig(config);
        config.addString("key", () -> this.key, value -> this.key = value, this.key).setDisplayName(new TextComponentTranslation("extraquests.key_value.key"));
        config.addLong("value", () -> this.value, value -> this.value = value, 5L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("extraquests.key_value.value"));
        config.addBool("ignore", () -> this.ignore, value -> this.ignore = value, false).setDisplayName(new TextComponentTranslation("extraquests.key_value.ignore"));
    }

    @Override
    public void claim(@Nonnull EntityPlayerMP player, boolean notify) {
        QuestData data = ServerQuestFile.INSTANCE.getData(player);

        if (data == null) {
            return;
        }

        for (KeyValueTask task : ServerQuestFile.INSTANCE.collect(KeyValueTask.class)) {
            TaskData taskData = data.getTaskData(task);

            if (taskData.progress < task.getMaxProgress()) {
                if (!this.ignore) {
                    ((KeyValueTask.Data) taskData).progress(this.key, this.value);
                } else if (task.quest.canStartTasks(data)) {
                    ((KeyValueTask.Data) taskData).progress(this.key, this.value);
                }
            }
        }
    }
}