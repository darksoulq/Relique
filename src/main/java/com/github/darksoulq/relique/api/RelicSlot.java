package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordBuilder;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public record RelicSlot(int order, String icon, int size, DropRule dropRule, List<Key> validators) {
    public static final Codec<RelicSlot> CODEC = RecordBuilder.create(instance -> instance.group(
        Codecs.INT.optionalFieldOf("order", 0).forGetter(RelicSlot::order),
        Codecs.STRING.optionalFieldOf("icon", "").forGetter(RelicSlot::icon),
        Codecs.INT.optionalFieldOf("size", 1).forGetter(RelicSlot::size),
        Codec.enumCodec(DropRule.class).optionalFieldOf("drop_rule", DropRule.DEFAULT).forGetter(RelicSlot::dropRule),
        Codecs.KEY.list().optionalFieldOf("validators", List.of(Key.key("relique:tag"))).forGetter(RelicSlot::validators)
    ).apply(instance, RelicSlot::new));
}