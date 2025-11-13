package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.server.event.EventBus;
import com.github.darksoulq.relique.api.event.RelicEquipEvent;
import com.github.darksoulq.relique.data.PlayerData;
import com.github.darksoulq.relique.data.Slots;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Handles equipping, unequipping, and querying relics on players.
 * <p>
 * This manager provides all high-level operations for managing player relics,
 * including firing {@link RelicEquipEvent} and invoking relic lifecycle callbacks
 * ({@link Relic#onEquip(Player, ItemStack)} and {@link Relic#onUnEquip(Player, ItemStack)}).
 */
public final class RelicManager {
    private RelicManager() {}

    /**
     * Attempts to equip a relic item into the first available slot of its type for the given player.
     * <p>
     * This will trigger a {@link RelicEquipEvent}, and if not cancelled, will call
     * {@link Relic#onEquip(Player, ItemStack)} for the relic.
     *
     * @param player the player to equip the relic to
     * @param item   the relic item to equip
     * @return {@code true} if successfully equipped, {@code false} if no slot is available or the item is invalid
     */
    public static boolean equip(Player player, ItemStack item) {
        if (!RelicItem.isRelic(item)) return false;
        Optional<Relic> relic = RelicItem.getRelic(item);
        if (relic.isEmpty()) return false;

        Slots.Slot slotType = relic.get().getSlot();
        PlayerData data = PlayerData.get(player);

        int index = data.firstEmpty(slotType);
        if (index == -1) return false;

        RelicEquipEvent event = EventBus.post(new RelicEquipEvent(item, slotType, relic.get(), index));
        if (event.isCancelled()) return false;
        ItemStack wear = item.clone();
        data.set(slotType, index, wear);
        data.save();
        relic.get().onEquip(player, wear);
        return true;
    }

    /**
     * Attempts to equip a relic item into a specific slot index.
     * <p>
     * If the target slot already contains a relic, it will be unequipped first
     * (triggering {@link Relic#onUnEquip(Player, ItemStack)}).
     * <p>
     * A {@link RelicEquipEvent} will be fired before equipping the new relic.
     *
     * @param player the player to equip the relic to
     * @param item   the relic item to equip
     * @param index  the target slot index (must be within slot capacity)
     * @return {@code true} if the relic was equipped successfully, {@code false} otherwise
     */
    public static boolean equip(Player player, ItemStack item, int index) {
        if (!RelicItem.isRelic(item)) return false;
        Optional<Relic> relic = RelicItem.getRelic(item);
        if (relic.isEmpty()) return false;

        Slots.Slot slotType = relic.get().getSlot();
        if (index < 0 || index >= slotType.getAmount()) return false;

        PlayerData data = PlayerData.get(player);
        Optional<ItemStack> old = data.get(slotType, index);

        old.flatMap(RelicItem::getRelic).ifPresent(r -> r.onUnEquip(player, old.get()));

        RelicEquipEvent event = EventBus.post(new RelicEquipEvent(item, slotType, relic.get(), index));
        if (event.isCancelled()) return false;
        ItemStack wear = item.clone();
        data.set(slotType, index, wear);
        data.save();

        relic.get().onEquip(player, wear);
        return true;
    }

    /**
     * Unequips the relic currently equipped in the given slot index.
     * <p>
     * Triggers {@link RelicEquipEvent} and, if not cancelled, calls
     * {@link Relic#onUnEquip(Player, ItemStack)}.
     *
     * @param player the player owning the relic
     * @param slot   the slot type
     * @param index  the index of the slot to clear
     */
    public static void unequip(Player player, Slots.Slot slot, int index) {
        PlayerData data = PlayerData.get(player);
        data.get(slot, index).ifPresent(stack -> {
            Optional<Relic> relic = RelicItem.getRelic(stack);
            if (relic.isEmpty()) return;
            RelicEquipEvent event = EventBus.post(new RelicEquipEvent(stack, relic.get().getSlot(), relic.get(), index));
            if (event.isCancelled()) return;
            relic.get().onUnEquip(player, stack);
            data.set(slot, index, slot.getDisplay());
        });
        data.save();
    }

    /**
     * Gets the {@link Relic} equipped in the specified slot and index, if any.
     *
     * @param player the player to check
     * @param slot   the slot type
     * @param index  the slot index
     * @return an {@link Optional} containing the equipped relic, or empty if none
     */
    public static Optional<Relic> getRelic(Player player, Slots.Slot slot, int index) {
        Optional<ItemStack> item = PlayerData.get(player).get(slot, index);
        if (item.isEmpty()) return Optional.empty();
        return RelicItem.getRelic(item.get());
    }

    /**
     * Returns all items (relics and empty placeholders (placeholders are always {@link Slots.Slot#getDisplay()})) in the given slot type for the player.
     *
     * @param player the player to query
     * @param slot   the slot type
     * @return a list of {@link ItemStack} representing the slot contents
     */
    public static List<ItemStack> getAllItems(Player player, Slots.Slot slot) {
        return PlayerData.get(player).getAll(slot);
    }

    /**
     * Checks whether the player has at least one free slot for the given slot type.
     *
     * @param player the player to check
     * @param slot   the slot type
     * @return {@code true} if there is an available slot, {@code false} if all are full
     */
    public static boolean hasFreeSlot(Player player, Slots.Slot slot) {
        return !PlayerData.get(player).isFull(slot);
    }
}
