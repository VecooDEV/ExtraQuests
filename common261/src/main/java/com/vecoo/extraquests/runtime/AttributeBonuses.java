package com.vecoo.extraquests.runtime;

import com.vecoo.extraquests.ExtraQuestsConstants;
import com.vecoo.extraquests.data.QuestFeatureData;
import com.vecoo.extraquests.internal.ExtraQuestsLogger;
import com.vecoo.extraquests.util.QuestMath;
import lombok.val;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;

public class AttributeBonuses {
    private static final Logger LOGGER = ExtraQuestsLogger.getLogger();

    @NotNull
    private static Identifier modifierId(@NotNull String reward) {
        return Identifier.fromNamespaceAndPath(ExtraQuestsConstants.MOD_ID, "reward/" + reward);
    }

    public static boolean grant(@NotNull ServerPlayer player, long reward, @NotNull String attribute, double amount, double maximum, int operation, int stacking, long duration) {
        val id = Identifier.tryParse(attribute);
        val holder = id == null ? null : BuiltInRegistries.ATTRIBUTE.get(id).orElse(null);

        if (holder == null || player.getAttribute(holder) == null || !Double.isFinite(amount) || !Double.isFinite(maximum)) {
            LOGGER.warn("Cannot apply attribute reward {}: invalid or unavailable attribute {}.", Long.toUnsignedString(reward, 16), attribute);

            return false;
        }

        refresh(player, System.currentTimeMillis());

        val saved = QuestFeatureData.get(player.level().getServer());
        val bonuses = saved.getPlayer(player.getUUID()).getCompoundOrEmpty("bonuses");
        val key = Long.toUnsignedString(reward, 16);
        val previous = bonuses.getCompoundOrEmpty(key);
        val compatible = attribute.equals(previous.getStringOr("attribute", "")) && operation == previous.getIntOr("operation", 0);
        val oldAmount = compatible ? previous.getDoubleOr("amount", 0D) : 0D;
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
        val id = Identifier.tryParse(entry.getStringOr("attribute", ""));

        if (id != null) {
            BuiltInRegistries.ATTRIBUTE.get(id).ifPresent(holder -> {
                val instance = player.getAttribute(holder);

                if (instance != null) {
                    instance.removeModifier(modifierId(key));
                }
            });
        }
    }

    public static void refresh(@NotNull ServerPlayer player, long now) {
        val saved = QuestFeatureData.get(player.level().getServer());
        val bonuses = saved.getPlayer(player.getUUID()).getCompoundOrEmpty("bonuses");

        for (String key : new ArrayList<>(bonuses.keySet())) {
            val entry = bonuses.getCompoundOrEmpty(key);
            val expires = entry.getLongOr("expires", 0L);

            if (expires != 0L && expires <= now) {
                remove(player, key, entry);
                bonuses.remove(key);
                saved.setDirty();
                continue;
            }

            val id = Identifier.tryParse(entry.getStringOr("attribute", ""));

            if (id == null) {
                continue;
            }

            BuiltInRegistries.ATTRIBUTE.get(id).ifPresent(holder -> {
                val instance = player.getAttribute(holder);

                if (instance != null && !instance.hasModifier(modifierId(key))) {
                    instance.addTransientModifier(new AttributeModifier(modifierId(key), entry.getDoubleOr("amount", 0D),
                            AttributeModifier.Operation.values()[Math.clamp(entry.getIntOr("operation", 0), 0, 2)]));
                }
            });
        }

        saved.getPlayer(player.getUUID()).put("bonuses", bonuses);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
