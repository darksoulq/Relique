package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.placeholder.Placeholder;
import com.github.darksoulq.abyssallib.server.placeholder.PlaceholderContext;
import com.github.darksoulq.abyssallib.server.placeholder.PlaceholderResult;
import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.data.RelicHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RelicPlaceholders {

    public static final DeferredRegistry<Placeholder<?>> PLACEHOLDERS = DeferredRegistry.create(Registries.PLACEHOLDERS, Relique.PLUGIN_ID);

    public static final Placeholder<?> EQUIPPED = PLACEHOLDERS.register("equipped", id -> new Placeholder<>(id, Component.class) {
        @Override
        public PlaceholderResult<Component> resolve(PlaceholderContext context) {
            Player player = context.getPlayer();
            if (player == null) return PlaceholderResult.empty();
            if (!context.hasArgs()) return PlaceholderResult.error("Missing slot argument");

            String slotId = context.arg(0).asString();
            RelicHandler handler = RelicHandler.get(player);
            List<ItemStack> items = handler.getEquipped(slotId);

            if (context.argsCount() > 1) {
                int idx;
                try {
                    idx = Integer.parseInt(context.arg(1).asString());
                } catch (NumberFormatException e) {
                    return PlaceholderResult.error("Invalid index");
                }

                if (idx < 0 || idx >= items.size()) return PlaceholderResult.empty();

                ItemStack item = items.get(idx);
                if (item == null || item.isEmpty()) return PlaceholderResult.empty();

                return PlaceholderResult.success(item.displayName().hoverEvent(item.asHoverEvent()));
            }

            Component result = Component.empty();
            boolean first = true;
            for (ItemStack item : items) {
                if (item != null && !item.isEmpty()) {
                    if (!first) result = result.append(Component.text(", "));
                    result = result.append(item.displayName().hoverEvent(item.asHoverEvent()));
                    first = false;
                }
            }

            return first ? PlaceholderResult.empty() : PlaceholderResult.success(result);
        }
    });

    public static final Placeholder<?> LIMIT = PLACEHOLDERS.register("limit", id -> new Placeholder<>(id, Integer.class) {
        @Override
        public PlaceholderResult<Integer> resolve(PlaceholderContext context) {
            Player player = context.getPlayer();
            if (player == null) return PlaceholderResult.empty();
            if (!context.hasArgs()) return PlaceholderResult.error("Missing slot argument");

            String slotId = context.arg(0).asString();
            return PlaceholderResult.success(RelicHandler.get(player).getSlotLimit(slotId));
        }
    });
}