package com.github.darksoulq.relique;

import com.github.darksoulq.abyssallib.common.util.Identifier;
import com.github.darksoulq.abyssallib.server.command.CommandBus;
import com.github.darksoulq.abyssallib.server.event.EventBus;
import com.github.darksoulq.relique.core.Events;
import com.github.darksoulq.relique.core.Items;
import com.github.darksoulq.relique.core.ReliqueCommands;
import com.github.darksoulq.relique.data.Slots;
import org.bukkit.plugin.java.JavaPlugin;

public final class Relique extends JavaPlugin {
    public static String PLUGIN_ID = "relique";
    public static Relique INSTANCE;

    public static Slots.Slot HEAD;
    public static Slots.Slot CHEST;
    public static Slots.Slot BELT;
    public static Slots.Slot RING;
    public static Slots.Slot CHARM;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Items.ITEMS.apply();

        EventBus bus = new EventBus(this);
        bus.register(new Events());

        Slots.load();
        registerDefaults();
        CommandBus.register(PLUGIN_ID, new ReliqueCommands());
    }

    @Override
    public void onDisable() {
        Slots.save();
    }

    private void registerDefaults() {
        HEAD = Slots.register("head", 1, Items.HEAD.get().getStack());
        CHEST = Slots.register("chest", 1, Items.CHEST.get().getStack());
        BELT = Slots.register("belt", 1, Items.BELT.get().getStack());
        RING = Slots.register("ring", 2, Items.RING.get().getStack());
        CHARM = Slots.register("charm", 2, Items.CHARM.get().getStack());
    }
}
