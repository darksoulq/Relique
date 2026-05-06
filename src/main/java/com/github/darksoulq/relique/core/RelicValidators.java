package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry;
import com.github.darksoulq.abyssallib.world.data.tag.Tag;
import com.github.darksoulq.abyssallib.world.data.tag.impl.ItemTag;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.api.RelicValidator;

public class RelicValidators {
    public static final DeferredRegistry<RelicValidator> VALIDATORS = DeferredRegistry.create(RelicRegistries.VALIDATORS, Relique.PLUGIN_ID);

    public static final RelicValidator TAG = VALIDATORS.register("tag", id -> (slotId, item, entity) -> {
        Tag<?, ?> tag = RelicTags.TAGS_MAP.get(slotId);
        if (tag instanceof ItemTag it) return it.contains(item);
        return true;
    });
}