package com.github.darksoulq.relique.data;

import com.github.darksoulq.abyssallib.common.config.Config;
import com.github.darksoulq.relique.Relique;

public class PluginConfig {
    public final Config config;

    public final Config.Value<Boolean> metrics;

    public PluginConfig() {
        config = new Config(Relique.PLUGIN_ID, "config");
        metrics = config.value("metrics", true);
    }
}
