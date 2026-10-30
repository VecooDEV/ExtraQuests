package com.vecoo.extraquests;

import com.vecoo.extraquests.quest.QuestSettings;
import com.vecoo.extraquests.task.TaskSettings;
import com.vecoo.extraquests.util.QuestJsonOps;
import de.marhali.json5.Json5Element;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import io.netty.buffer.Unpooled;
import lombok.val;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Json5NetworkTest {
    private CompoundTag transmit(CompoundTag tag) {
        val json = CompoundTag.CODEC.encodeStart(QuestJsonOps.INSTANCE, tag).getOrThrow();
        val buffer = Unpooled.buffer();

        try {
            Json5NetPacker.CODEC.encode(buffer, json);
            Json5Element decoded = Json5NetPacker.CODEC.decode(buffer);

            assertFalse(buffer.isReadable());

            return CompoundTag.CODEC.parse(Json5Ops.INSTANCE, decoded).getOrThrow();
        } finally {
            buffer.release();
        }
    }

    @Test
    void disabledTaskOptionsCanBeSentWithoutSerializingToText() {
        val settings = new TaskSettings();
        val restored = new TaskSettings();

        restored.read(transmit(settings.write()));

        assertEquals(settings.write(), restored.write());
    }

    @Test
    void enabledTaskOptionsPreserveBooleansEnumsAndLongValues() {
        val settings = new TaskSettings();

        settings.setShared(true);
        settings.setAggregation(TaskSettings.Aggregation.SUM);
        settings.setMinimumContribution(Long.MAX_VALUE);
        settings.setTimerEnabled(true);
        settings.setDuration(31536000L);
        settings.setTimerStart(TaskSettings.TimerStart.AVAILABLE);
        settings.setResetOnDeath(false);
        settings.setResetOnDamage(true);
        settings.setResetOnLogout(true);

        val restored = new TaskSettings();

        restored.read(transmit(settings.write()));

        assertEquals(settings.write(), restored.write());
    }

    @Test
    void calendarOptionsPreserveScheduleAndRotationSettings() {
        val settings = new QuestSettings();

        settings.setCalendarEnabled(true);
        settings.setWeekly(true);
        settings.setResetTime("07:30");
        settings.setTimezone("Asia/Magadan");
        settings.setWeekday(3);
        settings.setPool("daily");
        settings.setActiveQuests(2);

        val restored = new QuestSettings();

        restored.read(transmit(settings.write()));

        assertEquals(settings.write(), restored.write());
    }

    @Test
    void nestedByteAndShortValuesCanBePackedWithoutLosingNumericValues() {
        val tag = new CompoundTag();
        val nested = new CompoundTag();
        val values = new ListTag();

        nested.putByte("byte", Byte.MIN_VALUE);
        nested.putShort("short", Short.MAX_VALUE);
        nested.putLong("long", Long.MAX_VALUE);
        nested.putFloat("float", 1.5F);
        nested.putDouble("double", 1.1D);
        values.add(ByteTag.ZERO);
        values.add(ByteTag.ONE);
        tag.put("nested", nested);
        tag.put("values", values);
        tag.putByteArray("bytes", new byte[]{Byte.MIN_VALUE, 0, Byte.MAX_VALUE});

        val restored = transmit(tag);
        val restoredNested = restored.getCompoundOrEmpty("nested");

        assertEquals(Byte.MIN_VALUE, restoredNested.getByteOr("byte", (byte) 0));
        assertEquals(Short.MAX_VALUE, restoredNested.getShortOr("short", (short) 0));
        assertEquals(Long.MAX_VALUE, restoredNested.getLongOr("long", 0L));
        assertEquals(1.5F, restoredNested.getFloatOr("float", 0F));
        assertEquals(1.1D, restoredNested.getDoubleOr("double", 0D));
        assertEquals(values, restored.getListOrEmpty("values"));
        assertEquals(List.of(ByteTag.valueOf(Byte.MIN_VALUE), ByteTag.ZERO, ByteTag.valueOf(Byte.MAX_VALUE)), restored.getListOrEmpty("bytes"));
    }
}
