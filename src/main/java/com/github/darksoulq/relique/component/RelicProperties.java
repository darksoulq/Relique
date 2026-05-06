package com.github.darksoulq.relique.component;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.world.item.component.DataComponent;
import com.github.darksoulq.abyssallib.world.item.component.DataComponentType;

import java.util.ArrayList;
import java.util.List;

public class RelicProperties extends DataComponent<RelicProperties.Properties> {
    public record Properties(List<RelicAttributeModifier> attributes) {}

    public static final Codec<Properties> CODEC = RelicAttributeModifier.CODEC.list().orElse(new ArrayList<>()).xmap(Properties::new, Properties::attributes);

    public static final DataComponentType<RelicProperties> TYPE = DataComponentType.simple(
        CODEC.xmap(RelicProperties::new, RelicProperties::getValue)
    );

    public RelicProperties(Properties value) {
        super(value);
    }

    @Override
    public DataComponentType<?> getType() {
        return TYPE;
    }
}