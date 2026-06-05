package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordBuilder;

import java.util.List;

public record EntitySlotConfig(List<String> entities, List<String> slots) {
    public static final Codec<EntitySlotConfig> CODEC = RecordBuilder.create(instance -> instance.group(
        Codecs.STRING.list().fieldOf("entities").forGetter(EntitySlotConfig::entities),
        Codecs.STRING.list().fieldOf("slots").forGetter(EntitySlotConfig::slots)
    ).apply(instance, EntitySlotConfig::new));
}