package com.github.darksoulq.relique.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RelicUnequipEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final LivingEntity entity;
    private final String slotId;
    private final int index;
    private final ItemStack item;

    public RelicUnequipEvent(LivingEntity entity, String slotId, int index, ItemStack item) {
        this.entity = entity;
        this.slotId = slotId;
        this.index = index;
        this.item = item;
    }

    public LivingEntity getEntity() { return entity; }
    public String getSlotId() { return slotId; }
    public int getIndex() { return index; }
    public ItemStack getItem() { return item; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}