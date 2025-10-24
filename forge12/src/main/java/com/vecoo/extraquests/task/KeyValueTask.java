package com.vecoo.extraquests.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.vecoo.extraquests.integration.QuestsIntegration;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class KeyValueTask extends Task {
    private String key;
    private long value;

    public KeyValueTask(Quest quest) {
        super(quest);
        this.key = "key";
        this.value = 100L;
    }

    @Override
    @Nonnull
    public TaskType getType() {
        return QuestsIntegration.KEY_VALUE_TASK;
    }

    @Override
    @Nonnull
    public TaskData createData(@Nonnull QuestData questData) {
        return new Data(this, questData);
    }

    @Override
    public long getMaxProgress() {
        return this.value;
    }

    @Override
    public void writeData(@Nonnull NBTTagCompound nbt) {
        super.writeData(nbt);
        nbt.setString("key", this.key);
        nbt.setLong("value", this.value);
    }

    @Override
    public void readData(@Nonnull NBTTagCompound nbt) {
        super.readData(nbt);
        this.key = nbt.getString("key");
        this.value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(@Nonnull DataOut buffer) {
        super.writeNetData(buffer);
        buffer.writeString(this.key);
        buffer.writeVarLong(this.value);
    }

    @Override
    public void readNetData(@Nonnull DataIn buffer) {
        super.readNetData(buffer);
        this.key = buffer.readString();
        this.value = buffer.readVarLong();
    }

    public String getKey() {
        return this.key;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    @Nonnull
    public String getAltTitle() {
        return I18n.format("extraquests.key_value.title", this.key, this.value);
    }

    @Override
    @Nonnull
    public String getButtonText() {
        return String.valueOf(this.value);
    }

    @Override
    public void getConfig(@Nonnull ConfigGroup config) {
        super.getConfig(config);
        config.addString("key", () -> this.key, value -> this.key = value, this.key).setDisplayName(new TextComponentTranslation("extraquests.key_value.key"));
        config.addLong("value", () -> this.value, value -> this.value = value, 100L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("extraquests.key_value.value"));
    }

    public class Data extends TaskData<KeyValueTask> {
        public Data(KeyValueTask task, QuestData data) {
            super(task, data);
        }

        public void progress(String key, long value) {
            if (getKey().equals(key)) {
                addProgress(value);
            }
        }
    }
}