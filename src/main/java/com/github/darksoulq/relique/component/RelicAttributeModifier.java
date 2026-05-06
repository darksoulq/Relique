package com.github.darksoulq.relique.component;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordCodecBuilder;
import net.kyori.adventure.key.Key;
import org.bukkit.attribute.AttributeModifier.Operation;

import java.util.ArrayList;
import java.util.List;

public record RelicAttributeModifier(Key attribute, Key id, double amount, Operation operation, List<String> slots) {
    public static final Codec<RelicAttributeModifier> CODEC = RecordCodecBuilder.create(
        Codecs.KEY.fieldOf("attribute", RelicAttributeModifier::attribute),
        Codecs.KEY.fieldOf("id", RelicAttributeModifier::id),
        Codecs.DOUBLE.fieldOf("amount", RelicAttributeModifier::amount),
        Codec.enumCodec(Operation.class).fieldOf("operation", RelicAttributeModifier::operation),
        Codecs.STRING.list().orElse(new ArrayList<>()).fieldOf("slots", RelicAttributeModifier::slots),
        RelicAttributeModifier::new
    );
}