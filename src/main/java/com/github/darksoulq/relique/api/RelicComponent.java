package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.util.Identifier;
import com.github.darksoulq.abyssallib.world.item.Item;
import com.github.darksoulq.abyssallib.world.item.component.DataComponent;
import com.github.darksoulq.relique.Relique;

/**
 * A {@link DataComponent} that binds a specific {@link Relic} to an {@link Item}.
 * <p>
 * This allows {@link Item}s to carry information about which relic they represent,
 * enabling the game to restore relic functionality when the item is loaded.
 */
public class RelicComponent extends DataComponent<Relic> {
    /**
     * The codec used to serialize and deserialize {@link RelicComponent} instances.
     */
    private static final Codec<RelicComponent> CODEC = Codecs.KEY.xmap(
            v -> new RelicComponent(Relic.RELICS.get(v.asString())),
            d -> d.value.getId().asKey()
    );

    /**
     * Creates a new relic component for the given relic.
     *
     * @param relic the {@link Relic} instance to associate with this component
     */
    public RelicComponent(Relic relic) {
        super(Identifier.of(Relique.PLUGIN_ID, "relic"), relic, CODEC);
    }
}
