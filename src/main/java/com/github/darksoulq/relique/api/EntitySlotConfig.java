package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordCodecBuilder;

import java.util.List;

public record EntitySlotConfig(List<String> entities, List<String> slots) {
    public static final Codec<EntitySlotConfig> CODEC = RecordCodecBuilder.create(
        Codecs.STRING.list().fieldOf("entities", EntitySlotConfig::entities),
        Codecs.STRING.list().fieldOf("slots", EntitySlotConfig::slots),
        EntitySlotConfig::new
    );
}