package com.github.darksoulq.relique;

import com.github.darksoulq.abyssallib.server.command.CommandBus;
import com.github.darksoulq.abyssallib.server.event.EventBus;
import com.github.darksoulq.relique.core.*;
import com.github.darksoulq.relique.data.PluginConfig;
import com.github.darksoulq.relique.data.Resources;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Relique extends JavaPlugin {
    public static final String PLUGIN_ID = "relique";
    public static Relique INSTANCE;
    public static PluginConfig CONFIG;
    private final Metrics metrics = BukkitMetrics.factory()
        .token("af06c9aec3be1e546f1f096c99f4de0d")
        .create(this);

    @Override
    public void onEnable() {
        INSTANCE = this;
        CONFIG = new PluginConfig();

        PluginPermissions.NAMESPACE.apply();
        RelicComponents.COMPONENTS.apply();
        RelicTags.TAGS.apply();
        Items.ITEMS.apply();
        RelicValidators.VALIDATORS.apply();
        RelicPlaceholders.PLACEHOLDERS.apply();
        CommandBus.register(PLUGIN_ID, new InternalCommands());

        RelicLoader.clear();
        RelicLoader.loadResource(this);
        RelicLoader.load();

        Bukkit.getScheduler().runTaskLater(this, Resources::setupAndRegister, 15L);

        if (CONFIG.metrics.get()) metrics.ready();
        new EventBus(this).register(new Events());
    }
}