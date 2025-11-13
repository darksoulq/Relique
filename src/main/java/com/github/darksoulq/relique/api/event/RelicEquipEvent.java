package com.github.darksoulq.relique.api.event;

import com.github.darksoulq.relique.api.Relic;
import com.github.darksoulq.relique.data.Slots;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RelicEquipEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack item;
    private final Slots.Slot slot;
    private final Relic relic;
    private final int slotIndex;
    private boolean cancelled = false;

    public RelicEquipEvent(ItemStack item, Slots.Slot slot, Relic relic, int slotIndex) {
        this.item = item;
        this.slot = slot;
        this.relic = relic;
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public ItemStack getItem() {
        return item;
    }
    public Slots.Slot getSlot() {
        return slot;
    }
    public Relic getRelic() {
        return relic;
    }
    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
