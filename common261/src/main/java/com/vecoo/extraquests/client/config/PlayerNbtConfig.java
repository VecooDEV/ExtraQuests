package com.vecoo.extraquests.client.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableNBT;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Icon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class PlayerNbtConfig extends EditableNBT {
    @Getter
    @RequiredArgsConstructor
    private enum Field {
        HEALTH("Health", FloatTag.valueOf(20F)),
        ABSORPTION("AbsorptionAmount", FloatTag.valueOf(0F)),
        FOOD("foodLevel", IntTag.valueOf(20)),
        SATURATION("foodSaturationLevel", FloatTag.valueOf(5F)),
        EXHAUSTION("foodExhaustionLevel", FloatTag.valueOf(0F)),
        FOOD_TIMER("foodTickTimer", IntTag.valueOf(0)),
        XP_LEVEL("XpLevel", IntTag.valueOf(10)),
        XP_PROGRESS("XpP", FloatTag.valueOf(0F)),
        XP_TOTAL("XpTotal", IntTag.valueOf(0)),
        SCORE("Score", IntTag.valueOf(0)),
        AIR("Air", ShortTag.valueOf((short) 300)),
        FIRE("Fire", ShortTag.valueOf((short) -20)),
        ON_GROUND("OnGround", ByteTag.ONE),
        FALL_DISTANCE("fall_distance", DoubleTag.valueOf(0D)),
        HURT_TIME("HurtTime", ShortTag.valueOf((short) 0)),
        DEATH_TIME("DeathTime", ShortTag.valueOf((short) 0)),
        SLEEP_TIMER("SleepTimer", ShortTag.valueOf((short) 0)),
        FALL_FLYING("FallFlying", ByteTag.ZERO),
        DIMENSION("Dimension", StringTag.valueOf("minecraft:overworld")),
        GAME_MODE("playerGameType", IntTag.valueOf(0)),
        SEEN_CREDITS("seenCredits", ByteTag.ZERO),
        INVULNERABLE("Invulnerable", ByteTag.ZERO);

        @NotNull
        private final String key;
        @NotNull
        private final Tag defaultValue;

        @NotNull
        public Component getName() {
            return Component.translatable("extraquests.player_nbt.field." + this.key).append(" (" + this.key + ")");
        }
    }

    @Override
    public void onClicked(@NotNull Widget widget, @NotNull MouseButton button, @NotNull ConfigCallback callback) {
        if (!getCanEdit()) {
            return;
        }

        val screen = new AbstractButtonListScreen() {
            @Override
            public void addButtons(@NotNull Panel panel) {
                ConfigCallback onChanged = changed -> {
                    if (changed) {
                        panel.refreshWidgets();
                    }

                    callback.save(changed);
                };

                panel.add(SimpleTextButton.create(panel, Component.translatable("extraquests.player_nbt.manual"), Icon.empty(),
                        mouseButton -> PlayerNbtConfig.super.onClicked(panel, mouseButton, onChanged),
                        Component.translatable("extraquests.player_nbt.manual.tooltip")));

                for (val field : Field.values()) {
                    val value = getValue().get(field.getKey());
                    val title = value == null ? field.getName().copy().withStyle(ChatFormatting.GRAY)
                            : Component.literal("✓ ").append(field.getName()).append(" = " + StringUtil.truncateStringIfNecessary(value.toString(), 32, true))
                                    .withStyle(ChatFormatting.GREEN);
                    val state = value == null ? Component.translatable("extraquests.player_nbt.not_configured") : Component.translatable("extraquests.player_nbt.configured", value.toString());

                    panel.add(SimpleTextButton.create(panel, title, Icon.empty(), mouseButton -> editField(field, onChanged),
                            state, Component.translatable("extraquests.player_nbt.field.tooltip")));
                }
            }

            @Override
            protected void doAccept() {
                closeGui();
            }

            @Override
            protected void doCancel() {
                closeGui();
            }
        };

        screen.setTitle(Component.translatable("extraquests.player_nbt.fields"));
        screen.setHasSearchBox(true);
        screen.showBottomPanel(false);
        screen.showCloseButton(true);
        screen.openGui();
    }

    private void editField(@NotNull Field field, @NotNull ConfigCallback callback) {
        val draft = getValue().copy();
        val key = field.getKey();
        val group = new EditableConfigGroup("player_nbt_field", accepted -> {
            if (accepted) {
                callback.save(updateValue(draft));
            }
        }).setNameKey("extraquests.player_nbt.field_editor");
        val present = draft.get(key) != null && draft.get(key).getId() == field.getDefaultValue().getId();

        switch (field.getDefaultValue()) {
            case IntTag value -> group.addInt("value", present ? draft.getIntOr(key, 0) : value.intValue(), number -> draft.putInt(key, number),
                    value.intValue(), Integer.MIN_VALUE, Integer.MAX_VALUE).setNameKey("extraquests.player_nbt.value");
            case ShortTag value -> group.addInt("value", present ? draft.getShortOr(key, (short) 0) : value.intValue(), number -> draft.putShort(key, number.shortValue()),
                    value.intValue(), Short.MIN_VALUE, Short.MAX_VALUE).setNameKey("extraquests.player_nbt.value");
            case FloatTag value -> group.addDouble("value", present ? draft.getFloatOr(key, 0F) : value.doubleValue(), number -> draft.putFloat(key, number.floatValue()),
                    value.doubleValue(), -Float.MAX_VALUE, Float.MAX_VALUE).setNameKey("extraquests.player_nbt.value");
            case DoubleTag value -> group.addDouble("value", present ? draft.getDoubleOr(key, 0D) : value.doubleValue(), number -> draft.putDouble(key, number),
                    value.doubleValue(), -Double.MAX_VALUE, Double.MAX_VALUE).setNameKey("extraquests.player_nbt.value");
            case ByteTag value -> group.addBool("value", present ? draft.getBooleanOr(key, false) : value.byteValue() != 0, enabled -> draft.putBoolean(key, enabled),
                    value.byteValue() != 0).setNameKey("extraquests.player_nbt.value");
            case StringTag value -> group.addString("value", present ? draft.getStringOr(key, "") : value.value(), text -> draft.putString(key, text),
                    value.value()).setNameKey("extraquests.player_nbt.value");
            default -> throw new IllegalArgumentException("Unsupported player NBT field: " + key);
        }

        new EditConfigScreen(group) {
            @Override
            @NotNull
            public Component getTitle() {
                return field.getName();
            }
        }.setAutoclose(true).openGui();
    }
}
