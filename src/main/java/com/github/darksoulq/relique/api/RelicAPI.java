package com.github.darksoulq.relique.api;

import com.github.darksoulq.relique.core.RelicManager;
import com.github.darksoulq.relique.data.RelicHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class RelicAPI {

    private RelicAPI() {}

    public record SlotResult(String slotId, int index, ItemStack item) {}

    public static List<ItemStack> getEquipped(LivingEntity entity, String slotId) {
        List<ItemStack> original = RelicHandler.get(entity).getEquipped(slotId);
        List<ItemStack> copies = new ArrayList<>(original.size());
        for (ItemStack item : original) {
            copies.add(item != null && !item.isEmpty() ? item.clone() : ItemStack.empty());
        }
        return Collections.unmodifiableList(copies);
    }

    public static Optional<ItemStack> getEquipped(LivingEntity entity, String slotId, int index) {
        List<ItemStack> items = RelicHandler.get(entity).getEquipped(slotId);
        if (index >= 0 && index < items.size()) {
            ItemStack item = items.get(index);
            if (item != null && !item.isEmpty()) {
                return Optional.of(item.clone());
            }
        }
        return Optional.empty();
    }

    public static List<SlotResult> getAllEquipped(LivingEntity entity) {
        RelicHandler handler = RelicHandler.get(entity);
        List<SlotResult> results = new ArrayList<>();
        for (String slotId : RelicManager.getSlotsSorted()) {
            List<ItemStack> equipped = handler.getEquipped(slotId);
            for (int i = 0; i < equipped.size(); i++) {
                ItemStack item = equipped.get(i);
                if (item != null && !item.isEmpty()) {
                    results.add(new SlotResult(slotId, i, item.clone()));
                }
            }
        }
        return Collections.unmodifiableList(results);
    }

    public static int getSlotLimit(LivingEntity entity, String slotId) {
        return RelicHandler.get(entity).getSlotLimit(slotId);
    }

    public static void addSlotLimit(LivingEntity entity, String slotId, int amount) {
        RelicHandler.get(entity).addSlotLimit(slotId, amount);
    }

    public static void removeSlotLimit(LivingEntity entity, String slotId, int amount) {
        RelicHandler.get(entity).removeSlotLimit(slotId, amount);
    }

    public static void setSlotLimit(LivingEntity entity, String slotId, int amount) {
        RelicHandler.get(entity).setSlotLimit(slotId, amount);
    }

    public static boolean equip(LivingEntity entity, String slotId, int index, ItemStack item) {
        return RelicHandler.get(entity).equip(slotId, index, item);
    }

    public static ItemStack unequip(LivingEntity entity, String slotId, int index) {
        return RelicHandler.get(entity).unequip(slotId, index);
    }

    public static boolean canUnequip(LivingEntity entity, String slotId, int index) {
        return RelicHandler.get(entity).canUnequip(slotId, index, entity);
    }

    public static boolean isValid(LivingEntity entity, String slotId, ItemStack item) {
        return RelicManager.isValid(slotId, item, entity);
    }

    public static List<String> getAvailableSlots() {
        return RelicManager.getSlotsSorted();
    }

    public static RelicSlot getSlotDefinition(String slotId) {
        return RelicManager.getSlot(slotId);
    }
}