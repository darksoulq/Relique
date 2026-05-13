package com.github.darksoulq.relique.component;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.world.item.component.DataComponent;
import com.github.darksoulq.abyssallib.world.item.component.DataComponentType;

import java.util.ArrayList;
import java.util.List;

public class RelicProperties extends DataComponent<List<RelicAttributeModifier>> {

    public static final Codec<List<RelicAttributeModifier>> CODEC = RelicAttributeModifier.CODEC.list().orElse(new ArrayList<>());

    public static final DataComponentType<RelicProperties> TYPE = DataComponentType.simple(
        CODEC.xmap(RelicProperties::new, RelicProperties::getValue)
    );

    public RelicProperties(List<RelicAttributeModifier> value) {
        super(value);
    }

    @Override
    public DataComponentType<?> getType() {
        return TYPE;
    }
}