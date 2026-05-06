package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordCodecBuilder;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public record RelicSlot(int order, String icon, int size, DropRule dropRule, List<Key> validators) {
    public static final Codec<RelicSlot> CODEC = RecordCodecBuilder.create(
        Codecs.INT.orElse(0).fieldOf("order", RelicSlot::order),
        Codecs.STRING.orElse("").fieldOf("icon", RelicSlot::icon),
        Codecs.INT.orElse(1).fieldOf("size", RelicSlot::size),
        Codec.enumCodec(DropRule.class).orElse(DropRule.DEFAULT).fieldOf("drop_rule", RelicSlot::dropRule),
        Codecs.KEY.list().orElse(List.of(Key.key("relique:tag"))).fieldOf("validators", RelicSlot::validators),
        RelicSlot::new
    );
}