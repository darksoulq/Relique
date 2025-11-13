package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.event.ActionResult;
import com.github.darksoulq.abyssallib.world.data.tag.impl.ItemTag;
import com.github.darksoulq.abyssallib.world.gui.GuiElement;
import com.github.darksoulq.abyssallib.world.gui.GuiView;
import com.github.darksoulq.relique.api.Relic;
import com.github.darksoulq.relique.api.RelicItem;
import com.github.darksoulq.relique.api.RelicManager;
import com.github.darksoulq.relique.data.PlayerData;
import com.github.darksoulq.relique.data.Slots;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class RelicSlot implements GuiElement {
    private final Slots.Slot type;
    private final int index;
    public ItemStack stack;

    public RelicSlot(Slots.Slot type, int index, @Nullable ItemStack initial) {
        this.type = type;
        this.index = index;
        this.stack = initial == null ? new ItemStack(Material.PAPER) : initial;
    }

    @Override
    public @Nullable ItemStack render(GuiView view, int slot) {
        return stack;
    }

    @Override
    public ActionResult onClick(GuiView view, int slot, ClickType click, @Nullable ItemStack cursor, @Nullable ItemStack current) {
        Player player = (Player) view.getInventoryView().getPlayer();

        if (cursor == null || cursor.isEmpty()) {
            if (type.getDisplay().isSimilar(current)) return ActionResult.CANCEL;
            Optional<ItemStack> item = PlayerData.get(player).get(type, index);
            item.ifPresent(player::setItemOnCursor);
            RelicManager.unequip(player, type, index);
            this.stack = type.getDisplay().clone();
            return ActionResult.CANCEL;
        }

        if (cursor.isSimilar(type.getDisplay())) return ActionResult.CANCEL;
        ItemStack wear = cursor.clone();
        if (!RelicItem.isRelic(wear)) return ActionResult.CANCEL;
        Optional<Relic> relic = RelicItem.getRelic(wear);
        if (relic.isEmpty() || !relic.get().getSlot().getName().equals(type.getName())) return ActionResult.CANCEL;

        if (!stack.isSimilar(type.getDisplay())) player.setItemOnCursor(stack.clone());
        else player.setItemOnCursor(ItemStack.empty());
        RelicManager.equip(player, wear, index);
        this.stack = wear;
        return ActionResult.CANCEL;
    }

    @Override
    public ActionResult onDrag(GuiView view, Map<Integer, ItemStack> addedItems) {
        return ActionResult.CANCEL;
    }
}