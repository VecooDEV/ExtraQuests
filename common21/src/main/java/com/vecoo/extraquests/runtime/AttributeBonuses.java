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

import java.util.ArrayList;

public class AttributeBonuses {
    private static final Logger LOGGER = ExtraQuestsLogger.getLogger();

    @NotNull
    private static ResourceLocation modifierId(@NotNull String reward) {
        return ResourceLocation.fromNamespaceAndPath(ExtraQuestsConstants.MOD_ID, "reward/" + reward);
    }

    public static boolean grant(@NotNull ServerPlayer player, long reward, @NotNull String attribute, double amount, double maximum, int operation, int stacking, long duration) {
        val id = ResourceLocation.tryParse(attribute);
        val holder = id == null ? null : BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);

        if (holder == null || player.getAttribute(holder) == null || !Double.isFinite(amount) || !Double.isFinite(maximum)) {
            LOGGER.warn("Cannot apply attribute reward {}: invalid or unavailable attribute {}.", Long.toUnsignedString(reward, 16), attribute);

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
        entry.putDouble("amount", Math.clamp(next, -Math.abs(maximum), Math.abs(maximum)));
        entry.putInt("operation", Math.clamp(operation, 0, 2));
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
            BuiltInRegistries.ATTRIBUTE.getHolder(id).ifPresent(holder -> {
                val instance = player.getAttribute(holder);

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

            BuiltInRegistries.ATTRIBUTE.getHolder(id).ifPresent(holder -> {
                val instance = player.getAttribute(holder);

                if (instance != null && !instance.hasModifier(modifierId(key))) {
                    instance.addTransientModifier(new AttributeModifier(modifierId(key), entry.getDouble("amount"),
                            AttributeModifier.Operation.values()[Math.clamp(entry.getInt("operation"), 0, 2)]));
                }
            });
        }

        saved.getPlayer(player.getUUID()).put("bonuses", bonuses);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
