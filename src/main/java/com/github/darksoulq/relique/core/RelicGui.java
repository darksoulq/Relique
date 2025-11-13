package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.world.gui.*;
import com.github.darksoulq.abyssallib.world.gui.impl.GuiButton;
import com.github.darksoulq.abyssallib.world.gui.impl.PaginatedElements;
import com.github.darksoulq.relique.data.PlayerData;
import com.github.darksoulq.relique.data.Slots;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;

import java.util.ArrayList;
import java.util.List;

public class RelicGui {
    private static final int[] SLOTS = {
            1, 2, 3, 4, 5, 6, 7
    };

    public static void open(Player player) {
        PlayerData data = PlayerData.get(player);
        List<GuiElement> elems = new ArrayList<>();

        for (Slots.Slot type : Slots.all()) {
            List<ItemStack> equipped = data.getAll(type);
            int amount = type.getAmount();

            for (int i = 0; i < amount; i++) {
                ItemStack stack = (i < equipped.size()) ? equipped.get(i) : type.getDisplay().clone();
                elems.add(new RelicSlot(type, i, stack));
            }
        }

        PaginatedElements elements = new PaginatedElements(elems, SLOTS, GuiView.Segment.TOP);

        Gui gui = new Gui.Builder(MenuType.GENERIC_9X1, Component.text("Relics"))
                .addLayer(elements)
                .set(SlotPosition.top(0), GuiButton.of(Items.PREV.get().getStack(), (view, click) -> elements.prev(view)))
                .set(SlotPosition.top(8), GuiButton.of(Items.NEXT.get().getStack(), (view, click) -> elements.next(view)))
                .onClose(view -> data.save())
                .addFlag(GuiFlag.DISABLE_BOTTOM)
                .build();
        GuiManager.open(player, gui);
    }
}
