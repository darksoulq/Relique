package com.github.darksoulq.relique.core;

import com.github.darksoulq.relique.api.RelicSlot;
import com.github.darksoulq.relique.api.RelicValidator;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelicManager {
    private static final Map<String, RelicSlot> SLOTS = new ConcurrentHashMap<>();

    public static void clear() {
        SLOTS.clear();
    }

    public static void registerSlot(String id, RelicSlot slot) {
        SLOTS.put(id, slot);
    }

    public static RelicSlot getSlot(String id) {
        return SLOTS.get(id);
    }

    public static List<String> getSlotsSorted() {
        List<Map.Entry<String, RelicSlot>> list = new ArrayList<>(SLOTS.entrySet());
        list.sort(Comparator.comparingInt(e -> e.getValue().order()));
        List<String> sortedIds = new ArrayList<>();
        for (Map.Entry<String, RelicSlot> entry : list) {
            sortedIds.add(entry.getKey());
        }
        return sortedIds;
    }

    public static boolean isValid(String slotId, ItemStack item, LivingEntity entity) {
        if (item == null || item.isEmpty()) return true;
        RelicSlot slot = SLOTS.get(slotId);
        if (slot == null) return false;

        for (Key valKey : slot.validators()) {
            RelicValidator validator = RelicRegistries.VALIDATORS.get(valKey.asString());
            if (validator != null && validator.isValid(slotId, item, entity)) {
                return true;
            }
        }
        return false;
    }
}