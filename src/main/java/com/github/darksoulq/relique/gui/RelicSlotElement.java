package com.github.darksoulq.relique.gui;

import com.github.darksoulq.abyssallib.server.event.ActionResult;
import com.github.darksoulq.abyssallib.server.event.context.gui.GuiClickContext;
import com.github.darksoulq.abyssallib.server.event.context.gui.GuiDragContext;
import com.github.darksoulq.abyssallib.world.gui.GuiElement;
import com.github.darksoulq.abyssallib.world.gui.GuiView;
import com.github.darksoulq.relique.core.RelicManager;
import com.github.darksoulq.relique.data.RelicHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RelicSlotElement implements GuiElement {
    private final RelicHandler handler;
    private final String slotId;
    private final int slotIndex;
    private final ItemStack bgIcon;

    public RelicSlotElement(RelicHandler handler, String slotId, int slotIndex, ItemStack bgIcon) {
        this.handler = handler;
        this.slotId = slotId;
        this.slotIndex = slotIndex;
        this.bgIcon = bgIcon;
    }

    @Override
    public @Nullable ItemStack render(GuiView view, int slot) {
        ItemStack current = handler.getEquipped(slotId).size() > slotIndex ? handler.getEquipped(slotId).get(slotIndex) : null;
        if (current != null && !current.isEmpty()) {
            return current;
        }
        return bgIcon;
    }

    @Override
    public ActionResult onClick(GuiClickContext ctx) {
        if (ctx.clickType().isKeyboardClick() || ctx.clickType() == org.bukkit.event.inventory.ClickType.SWAP_OFFHAND) {
            return ActionResult.CANCEL;
        }

        Player player = (Player) ctx.source();
        ItemStack cursor = ctx.cursor();
        ItemStack current = handler.getEquipped(slotId).size() > slotIndex ? handler.getEquipped(slotId).get(slotIndex) : null;

        boolean hasCursor = cursor != null && !cursor.isEmpty() && cursor.getType() != Material.AIR;
        boolean hasCurrent = current != null && !current.isEmpty() && current.getType() != Material.AIR;

        if (ctx.action() == InventoryAction.COLLECT_TO_CURSOR) {
            return ActionResult.CANCEL;
        }

        if (ctx.action() == InventoryAction.DROP_ONE_SLOT || ctx.action() == InventoryAction.DROP_ALL_SLOT) {
            if (hasCurrent) {
                ItemStack drop = current.clone();
                int amount = ctx.action() == InventoryAction.DROP_ONE_SLOT ? 1 : drop.getAmount();
                drop.setAmount(amount);
                current.setAmount(current.getAmount() - amount);
                if (current.getAmount() <= 0) {
                    handler.unequip(slotId, slotIndex);
                } else {
                    handler.equip(slotId, slotIndex, current);
                }
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
                ctx.view().render();
            }
            return ActionResult.CANCEL;
        }

        if (ctx.clickType().isShiftClick()) {
            if (hasCurrent) {
                handler.unequip(slotId, slotIndex);
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(current.clone());
                if (!leftover.isEmpty()) {
                    handler.equip(slotId, slotIndex, leftover.get(0));
                }
                ctx.view().render();
            }
            return ActionResult.CANCEL;
        }

        if (hasCursor) {
            if (RelicManager.isValid(slotId, cursor, player)) {
                ItemStack toEquip = cursor.clone();
                toEquip.setAmount(1);

                if (hasCurrent) {
                    handler.unequip(slotId, slotIndex);
                    if (cursor.getAmount() == 1) {
                        ctx.view().getInventoryView().setCursor(current.clone());
                    } else {
                        cursor.setAmount(cursor.getAmount() - 1);
                        ctx.view().getInventoryView().setCursor(cursor);
                        player.getInventory().addItem(current.clone()).values().forEach(
                            leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover)
                        );
                    }
                } else {
                    cursor.setAmount(cursor.getAmount() - 1);
                    ctx.view().getInventoryView().setCursor(cursor.getAmount() <= 0 ? null : cursor);
                }
                handler.equip(slotId, slotIndex, toEquip);
                ctx.view().render();
            }
        } else if (hasCurrent) {
            ctx.view().getInventoryView().setCursor(current.clone());
            handler.unequip(slotId, slotIndex);
            ctx.view().render();
        }

        return ActionResult.CANCEL;
    }

    @Override
    public ActionResult onDrag(GuiDragContext ctx) {
        return ActionResult.CANCEL;
    }
}