package com.vecoo.extraquests.runtime;

import com.vecoo.extraquests.ExtraQuestsConstants;
import com.vecoo.extraquests.data.QuestFeatureData;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import com.vecoo.extraquests.util.QuestMath;
import lombok.val;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class AttributeBonuses {
    private static final Logger LOGGER = ExtraQuestsLogger.getLogger();

    @NotNull
    private static UUID modifierId(@NotNull String reward) {
        return UUID.nameUUIDFromBytes((ExtraQuestsConstants.MOD_ID + ":reward/" + reward).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean canGrant(@NotNull ServerPlayer player, long reward, @NotNull String attribute, double amount, double maximum) {
        val id = ResourceLocation.tryParse(attribute);
        val attributeType = id == null ? null : BuiltInRegistries.ATTRIBUTE.getOptional(id).orElse(null);

        if (attributeType == null || player.getAttribute(attributeType) == null || !Double.isFinite(amount) || !Double.isFinite(maximum)) {
            LOGGER.warn("Cannot apply attribute reward {}: invalid or unavailable attribute {}.", Long.toUnsignedString(reward, 16), attribute);

            return false;
        }

        return true;
    }

    public static boolean grant(@NotNull ServerPlayer player, long reward, @NotNull String attribute, double amount, double maximum, int operation, int stacking, long duration) {
        if (!canGrant(player, reward, attribute, amount, maximum)) {
            return false;
        }

        refresh(player, System.currentTimeMillis());

        val saved = QuestFeatureData.get(player.server);
        val bonuses = saved.getPlayer(player.getUUID()).getCompound("bonuses");
        val key = Long.toUnsignedString(reward, 16);
        val previous = bonuses.getCompound(key);
        val compatible = attribute.equals(previous.getString("attribute")) && operation == previous.getInt("operation");
        val oldAmount = compatible ? previous.getDouble("amount") : 0D;
        val next = switch (stacking) {
            case 1 -> oldAmount + amount;
            case 2 -> Math.abs(oldAmount) > Math.abs(amount) ? oldAmount : amount;
            default -> amount;
        };

        remove(player, key, previous);

        val entry = new CompoundTag();

        entry.putString("attribute", attribute);
        entry.putDouble("amount", Math.max(-Math.abs(maximum), Math.min(Math.abs(maximum), next)));
        entry.putInt("operation", Math.max(0, Math.min(2, operation)));
        entry.putLong("expires", duration <= 0L ? 0L : QuestMath.deadline(System.currentTimeMillis(), duration));
        bonuses.put(key, entry);
        saved.getPlayer(player.getUUID()).put("bonuses", bonuses);
        saved.setDirty();
        refresh(player, System.currentTimeMillis());

        return true;
    }

    private static void remove(@NotNull ServerPlayer player, @NotNull String key, @NotNull CompoundTag entry) {
        val id = ResourceLocation.tryParse(entry.getString("attribute"));

        if (id != null) {
            BuiltInRegistries.ATTRIBUTE.getOptional(id).ifPresent(attributeType -> {
                val instance = player.getAttribute(attributeType);

                if (instance != null) {
                    instance.removeModifier(modifierId(key));
                }
            });
        }
    }

    public static void refresh(@NotNull ServerPlayer player, long now) {
        val saved = QuestFeatureData.get(player.server);
        val bonuses = saved.getPlayer(player.getUUID()).getCompound("bonuses");

        for (String key : new ArrayList<>(bonuses.getAllKeys())) {
            val entry = bonuses.getCompound(key);
            val expires = entry.getLong("expires");

            if (expires != 0L && expires <= now) {
                remove(player, key, entry);
                bonuses.remove(key);
                saved.setDirty();
                continue;
            }

            val id = ResourceLocation.tryParse(entry.getString("attribute"));

            if (id == null) {
                continue;
            }

            BuiltInRegistries.ATTRIBUTE.getOptional(id).ifPresent(attributeType -> {
                val instance = player.getAttribute(attributeType);

                if (instance != null && instance.getModifier(modifierId(key)) == null) {
                    instance.addTransientModifier(new AttributeModifier(modifierId(key), "ExtraQuests reward " + key, entry.getDouble("amount"),
                            AttributeModifier.Operation.values()[Math.max(0, Math.min(2, entry.getInt("operation")))]));
                }
            });
        }

        saved.getPlayer(player.getUUID()).put("bonuses", bonuses);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
