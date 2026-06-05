package com.github.darksoulq.relique.component;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordBuilder;
import net.kyori.adventure.key.Key;
import org.bukkit.attribute.AttributeModifier.Operation;

import java.util.ArrayList;
import java.util.List;

public record RelicAttributeModifier(Key attribute, Key id, double amount, Operation operation, List<String> slots) {
    public static final Codec<RelicAttributeModifier> CODEC = RecordBuilder.create(instance -> instance.group(
        Codecs.KEY.fieldOf("attribute").forGetter(RelicAttributeModifier::attribute),
        Codecs.KEY.fieldOf("id").forGetter(RelicAttributeModifier::id),
        Codecs.DOUBLE.fieldOf("amount").forGetter(RelicAttributeModifier::amount),
        Codec.enumCodec(Operation.class).fieldOf("operation").forGetter(RelicAttributeModifier::operation),
        Codecs.STRING.list().optionalFieldOf("slots", new ArrayList<>()).forGetter(RelicAttributeModifier::slots)
    ).apply(instance, RelicAttributeModifier::new));
}