package com.github.darksoulq.relique.gui;

import com.github.darksoulq.abyssallib.common.util.TextUtil;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset;
import com.github.darksoulq.abyssallib.world.gui.*;
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton;
import com.github.darksoulq.abyssallib.world.gui.layer.PagedLayer;
import com.github.darksoulq.abyssallib.world.item.Item;
import com.github.darksoulq.relique.core.Items;
import com.github.darksoulq.relique.core.RelicManager;
import com.github.darksoulq.relique.data.RelicHandler;
import com.github.darksoulq.relique.data.Resources;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;

import java.util.ArrayList;
import java.util.List;

public class RelicGui {
    private static final int[] SLOTS = { 1, 2, 3, 4, 5, 6, 7 };

    public static void open(Player player) {
        RelicHandler data = RelicHandler.get(player);
        List<GuiElement> elems = new ArrayList<>();

        for (String slotId : RelicManager.getSlotsSorted()) {
            int amount = data.getSlotLimit(slotId);
            if (amount <= 0) continue;

            String iconId = RelicManager.getSlot(slotId).icon();
            ItemStack bgIcon = new ItemStack(Material.PAPER);

            if (iconId != null && !iconId.isEmpty()) {
                if (!iconId.contains(":")) {
                    Item item = Registries.ITEMS.get(iconId);
                    if (item != null) bgIcon = item.getStack().clone();
                } else {
                    String[] parts = iconId.split(":", 2);
                    if (parts[0].equals("minecraft")) {
                        Material m = Material.matchMaterial(parts[1]);
                        if (m != null) bgIcon = new ItemStack(m);
                    } else {
                        Item item = Registries.ITEMS.get(iconId);
                        if (item != null) bgIcon = item.getStack().clone();
                    }
                }
            }

            for (int i = 0; i < amount; i++) {
                elems.add(new RelicSlotElement(data, slotId, i, bgIcon));
            }
        }

        PagedLayer<GuiElement> layer = PagedLayer.of(elems, SLOTS, GuiView.Segment.TOP);

        Gui.Builder builder = Gui.builder(MenuType.GENERIC_9X1, TextUtil.parse("<white><offset><gui></white><offset2>Relics",
                Placeholder.parsed("offset", TextOffset.getOffsetMinimessage(-8)),
                Placeholder.parsed("gui", Resources.RELIC_GUI.toMiniMessageString()),
                Placeholder.parsed("offset2", TextOffset.getOffsetMinimessage(-170))))
            .addLayer(layer)
            .set(SlotPosition.top(0), new GuiButton(Items.PREV.getStack(), ctx -> {
                layer.previous(ctx.view());
            }))
            .set(SlotPosition.top(8), new GuiButton(Items.NEXT.getStack(), ctx -> {
                layer.next(ctx.view());
            }))
            .onOpen(layer::renderTo)
            .onClose(view -> data.save());

        PlayerInventoryElement bottomElement = new PlayerInventoryElement(data);
        for (int i = 0; i < 36; i++) {
            builder.set(SlotPosition.bottom(i), bottomElement);
        }

        GuiManager.open(player, builder.build());
    }
}