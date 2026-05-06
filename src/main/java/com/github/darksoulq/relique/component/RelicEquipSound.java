package com.github.darksoulq.relique.component;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordCodecBuilder;
import com.github.darksoulq.abyssallib.world.item.component.DataComponent;
import com.github.darksoulq.abyssallib.world.item.component.DataComponentType;
import net.kyori.adventure.key.Key;

public class RelicEquipSound extends DataComponent<RelicEquipSound.Sounds> {
    public record Sounds(Key equip, Key unequip) {}

    public static final Codec<Sounds> CODEC = RecordCodecBuilder.create(
        Codecs.KEY.orElse(null).fieldOf("equip", Sounds::equip),
        Codecs.KEY.orElse(null).fieldOf("unequip", Sounds::unequip),
        Sounds::new
    );

    public static final DataComponentType<RelicEquipSound> TYPE = DataComponentType.simple(
        CODEC.xmap(RelicEquipSound::new, RelicEquipSound::getValue)
    );

    public RelicEquipSound(Sounds value) {
        super(value);
    }

    @Override
    public DataComponentType<?> getType() {
        return TYPE;
    }
}