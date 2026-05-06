package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.world.item.component.DataComponentType;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.component.RelicEquipSound;
import com.github.darksoulq.relique.component.RelicProperties;

public class RelicComponents {
    public static final DeferredRegistry<DataComponentType<?>> COMPONENTS = DeferredRegistry.create(Registries.DATA_COMPONENT_TYPES, Relique.PLUGIN_ID);

    public static final DataComponentType<?> RELIC_PROPERTIES = COMPONENTS.register("relic_properties", id -> RelicProperties.TYPE);
    public static final DataComponentType<?> RELIC_SOUNDS = COMPONENTS.register("relic_sounds", id -> RelicEquipSound.TYPE);
}