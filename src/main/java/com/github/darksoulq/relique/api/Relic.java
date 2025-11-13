package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.common.util.Identifier;
import com.github.darksoulq.abyssallib.server.registry.Registry;
import com.github.darksoulq.relique.data.Slots;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a Relic — a special, equipable item that grants passive or active effects
 * when placed into a specific {@link Slots.Slot} by a player.
 * <p>
 * Relics are registered into a global {@link #RELICS} registry and can respond to
 * lifecycle events such as equip, unequip, and tick.
 */
public abstract class Relic {
    /**
     * The global registry containing all registered {@link Relic} instances.
     */
    public static final Registry<Relic> RELICS = new Registry<>();

    private final Identifier id;
    private final Slots.Slot slot;

    /**
     * Creates a new Relic definition.
     *
     * @param id   the unique {@link Identifier} of this relic (e.g. {@code relique:fire_amulet})
     * @param slot the {@link Slots.Slot} type this relic can be equipped in
     */
    public Relic(Identifier id, Slots.Slot slot) {
        this.id = id;
        this.slot = slot;
    }

    /**
     * Called when this relic is equipped by a player.
     *
     * @param wearer the player who equipped the relic
     * @param item   the {@link ItemStack} representing this relic
     */
    public abstract void onEquip(Player wearer, ItemStack item);
    /**
     * Called when this relic is unequipped by a player.
     *
     * @param wearer the player who unequipped the relic
     * @param item   the {@link ItemStack} representing this relic
     */
    public abstract void onUnEquip(Player wearer, ItemStack item);
    /**
     * Called every game tick while this relic remains equipped.
     *
     * @param wearer the player currently wearing the relic
     * @param item   the {@link ItemStack} representing this relic
     */
    public abstract void onTick(Player wearer, ItemStack item);

    /**
     * Gets the unique identifier of this relic.
     *
     * @return the relic's {@link Identifier}
     */
    public Identifier getId() {
        return id;
    }
    /**
     * Gets the slot type this relic can be equipped in.
     *
     * @return the {@link Slots.Slot} for this relic
     */
    public Slots.Slot getSlot() {
        return slot;
    }
}
