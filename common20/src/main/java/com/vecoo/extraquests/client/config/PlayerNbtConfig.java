package com.vecoo.extraquests.client.config;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NBTConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class PlayerNbtConfig extends NBTConfig {
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
        FALL_DISTANCE("FallDistance", FloatTag.valueOf(0F)),
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
        val group = new ConfigGroup("player_nbt_field", accepted -> {
            if (accepted) {
                callback.save(setCurrentValue(draft));
            }
        }).setNameKey("extraquests.player_nbt.field_editor");
        val present = draft.contains(key, field.getDefaultValue().getId());

        val defaultValue = field.getDefaultValue();

        if (defaultValue instanceof IntTag value) {
            group.addInt("value", present ? draft.getInt(key) : value.getAsInt(), number -> draft.putInt(key, number),
                    value.getAsInt(), Integer.MIN_VALUE, Integer.MAX_VALUE).setNameKey("extraquests.player_nbt.value");
        } else if (defaultValue instanceof ShortTag value) {
            group.addInt("value", present ? draft.getShort(key) : value.getAsInt(), number -> draft.putShort(key, number.shortValue()),
                    value.getAsInt(), Short.MIN_VALUE, Short.MAX_VALUE).setNameKey("extraquests.player_nbt.value");
        } else if (defaultValue instanceof FloatTag value) {
            group.addDouble("value", present ? draft.getFloat(key) : value.getAsDouble(), number -> draft.putFloat(key, number.floatValue()),
                    value.getAsDouble(), -Float.MAX_VALUE, Float.MAX_VALUE).setNameKey("extraquests.player_nbt.value");
        } else if (defaultValue instanceof ByteTag value) {
            group.addBool("value", present ? draft.getBoolean(key) : value.getAsByte() != 0, enabled -> draft.putBoolean(key, enabled),
                    value.getAsByte() != 0).setNameKey("extraquests.player_nbt.value");
        } else if (defaultValue instanceof StringTag value) {
            group.addString("value", present ? draft.getString(key) : value.getAsString(), text -> draft.putString(key, text),
                    value.getAsString()).setNameKey("extraquests.player_nbt.value");
        } else {
            throw new IllegalArgumentException("Unsupported player NBT field: " + key);
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
