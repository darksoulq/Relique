package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.world.data.tag.Tag;
import com.github.darksoulq.abyssallib.world.data.tag.impl.ItemTag;
import com.github.darksoulq.relique.Relique;
import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Map;

public class RelicTags {
    public static final DeferredRegistry<Tag<?, ?>> TAGS = DeferredRegistry.create(Registries.TAGS, Relique.PLUGIN_ID);
    public static final Map<String, Tag<?, ?>> TAGS_MAP = new HashMap<>();

    public static void register(String namespace, String id) {
        if (TAGS_MAP.containsKey(id)) return;
        Tag<?, ?> tag = new ItemTag(Key.key(namespace, id));
        Registries.TAGS.register(namespace + ":" + id, tag);
        TAGS_MAP.put(id, tag);
    }
}