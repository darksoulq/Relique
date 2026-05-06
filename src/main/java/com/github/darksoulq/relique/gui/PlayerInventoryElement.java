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

public class PlayerInventoryElement implements GuiElement {
    private final RelicHandler handler;

    public PlayerInventoryElement(RelicHandler handler) {
        this.handler = handler;
    }

    @Override
    public @Nullable ItemStack render(GuiView view, int slot) {
        return null;
    }

    @Override
    public ActionResult onClick(GuiClickContext ctx) {
        Player player = (Player) ctx.source();
        if (ctx.action() == InventoryAction.COLLECT_TO_CURSOR) {
            ItemStack cursor = ctx.cursor();
            if (cursor != null && !cursor.isEmpty() && cursor.getType() != Material.AIR) {
                int needed = cursor.getMaxStackSize() - cursor.getAmount();
                if (needed > 0) {
                    for (int i = 0; i < 36; i++) {
                        ItemStack item = ctx.view().getBottom().getItem(i);
                        if (item != null && item.isSimilar(cursor)) {
                            int take = Math.min(needed, item.getAmount());
                            cursor.setAmount(cursor.getAmount() + take);
                            item.setAmount(item.getAmount() - take);
                            ctx.view().getBottom().setItem(i, item.getAmount() <= 0 ? null : item);
                            needed -= take;
                            if (needed <= 0) break;
                        }
                    }
                    ctx.view().getInventoryView().setCursor(cursor);
                }
            }
            return ActionResult.CANCEL;
        }

        if (ctx.clickType().isShiftClick()) {
            ItemStack clicked = ctx.currentItem();
            if (clicked != null && !clicked.isEmpty() && clicked.getType() != Material.AIR) {
                for (String slotId : RelicManager.getSlotsSorted()) {
                    if (RelicManager.isValid(slotId, clicked, player)) {
                        int limit = handler.getSlotLimit(slotId);
                        for (int i = 0; i < limit; i++) {
                            ItemStack equipped = handler.getEquipped(slotId).size() > i ? handler.getEquipped(slotId).get(i) : null;

                            if (equipped == null || equipped.isEmpty() || equipped.getType() == Material.AIR) {
                                ItemStack toEquip = clicked.clone();
                                toEquip.setAmount(1);

                                if (handler.equip(slotId, i, toEquip)) {
                                    clicked.setAmount(clicked.getAmount() - 1);
                                    ctx.view().getBottom().setItem(ctx.slot(), clicked.getAmount() <= 0 ? null : clicked);
                                    ctx.view().render();
                                    return ActionResult.CANCEL;
                                }
                            }
                        }
                    }
                }
            }
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Override
    public ActionResult onDrag(GuiDragContext ctx) {
        return ActionResult.PASS;
    }
}