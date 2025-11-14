package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.server.registry.Registry;
import com.github.darksoulq.abyssallib.server.registry.object.Holder;
import com.github.darksoulq.abyssallib.world.item.Item;
import com.github.darksoulq.relique.Relique;
import org.bukkit.Material;

public class Items {
    public static final DeferredRegistry<Item> ITEMS = DeferredRegistry.create(Registries.ITEMS, Relique.PLUGIN_ID);

    public static final Holder<Item> CHARM  = create("charm", true);
    public static final Holder<Item> BELT = create("belt", true);
    public static final Holder<Item> RING = create("ring", true);
    public static final Holder<Item> HEAD = create("head", true);
    public static final Holder<Item> CHEST = create("chest", true);

    public static final Holder<Item> NEXT = create("next", false);
    public static final Holder<Item> PREV = create("prev", false);
    public static final Holder<Item> UP = create("up", false);
    public static final Holder<Item> DOWN = create("down", false);

    private static Holder<Item> create(String name, boolean hideTooltip) {
        return ITEMS.register(name, id -> {
            Item item = new Item(id, Material.PAPER);
            if (!hideTooltip) return item;
            item.tooltip.setVisible(false);
            item.updateTooltip();
            return item;
        });
    }
}
