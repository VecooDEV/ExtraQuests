package com.vecoo.extraquests.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeyValueTask extends Task {
    public static TaskType TYPE;

    private String key = "key";
    private long value = 100L;

    public KeyValueTask(Quest quest) {
        super(quest);
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public TaskData createData(QuestData questData) {
        return new Data(this, questData);
    }

    @Override
    public long getMaxProgress() {
        return value;
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
    public void writeNetData(DataOut data) {
        super.writeNetData(data);
        data.writeString(key);
        data.writeVarLong(value);
    }

    @Override
    public void readNetData(DataIn buffer) {
        super.readNetData(buffer);
        key = buffer.readString();
        value = buffer.readVarLong();
    }

    public String getKey() {
        return this.key;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public String getAltTitle() {
        return I18n.format("extraquests.key_value.title", getKey(), getValue());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("key", () -> key, v -> key = v, "").setDisplayName(new TextComponentTranslation("extraquests.key_value.key"));
        config.addLong("value", () -> value, v -> value = v, 100L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("extraquests.key_value.value"));
    }

    public class Data extends TaskData<KeyValueTask> {
        public Data(KeyValueTask task, QuestData data) {
            super(task, data);
        }

        public void prgoress(String key, long value) {
            if (!getKey().equals(key)) {
                return;
            }

            addProgress(value);
        }
    }
}