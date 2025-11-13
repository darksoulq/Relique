package com.github.darksoulq.relique.api;

import com.github.darksoulq.abyssallib.common.util.CTag;
import com.github.darksoulq.abyssallib.common.util.TextUtil;
import com.github.darksoulq.relique.Relique;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for handling items that represent or contain {@link Relic} data.
 * <p>
 * Provides methods for tagging, identifying, and decorating relic items using
 * NBT ({@link CTag}) and {@link DataComponentTypes#LORE}.
 */
public class RelicItem {
    /**
     * Default lore line applied to relics when {@link RelicItem#applyDefaultLore(ItemStack)} is called.
     */
    private static final String DEFAULT_LORE = "<!italic><gold>Slot:</gold> <yellow><slot_name></yellow>";

    /**
     * Checks whether the given {@link ItemStack} is marked as a relic.
     *
     * @param stack the item to check
     * @return {@code true} if the item contains a relic tag, {@code false} otherwise
     */
    public static boolean isRelic(ItemStack stack) {
        if (stack == null) return false;
        CTag tag = getCTag(stack);
        return tag.has(Relique.PLUGIN_ID + ":relic");
    }
    /**
     * Applies a relic tag to the given item, marking it as a relic.
     *
     * @param stack the target item
     * @param relic the {@link Relic} to bind to this item
     * @return {@code true} if the tag was successfully applied; {@code false} if the item was already a relic
     */
    public static boolean applyRelic(ItemStack stack, Relic relic) {
        if (stack == null) return false;
        CTag tag = getCTag(stack);
        if (tag.has(Relique.PLUGIN_ID + ":relic")) return false;
        tag.set(Relique.PLUGIN_ID + ":relic", relic.getId().toString());
        setCTag(tag, stack);
        return true;
    }
    /**
     * Adds a default lore line to the given relic item if it is a relic
     * <p>
     * The line includes the slot name
     * as {@code relique.slot.<slot_name>}.
     *
     * @param stack the relic item to modify
     * @return {@code true} if lore was applied successfully; {@code false} if the item is not a relic
     */
    public static boolean applyDefaultLore(ItemStack stack) {
        if (!isRelic(stack)) return false;
        Optional<Relic> relic = getRelic(stack);
        if (relic.isEmpty()) return false;
        ItemLore oldLore = stack.getData(DataComponentTypes.LORE);
        ItemLore.Builder builder = ItemLore.lore();
        builder.addLine(TextUtil.parse(DEFAULT_LORE, Placeholder.parsed("slot_name", "<lang:relique.slot." + relic.get().getSlot().getName() + ">")));
        builder.addLines(oldLore.lines());
        stack.setData(DataComponentTypes.LORE, builder.build());
        return true;
    }
    /**
     * Retrieves the {@link Relic} associated with this item, if any.
     *
     * @param stack the item to inspect
     * @return an {@link Optional} containing the {@link Relic} if found, or empty if not
     */
    public static Optional<Relic> getRelic(ItemStack stack) {
        if (stack == null) return Optional.empty();
        CTag tag = getCTag(stack);
        if (!tag.has( Relique.PLUGIN_ID + ":relic")) return Optional.empty();
        return Optional.ofNullable(Relic.RELICS.get(tag.getString(Relique.PLUGIN_ID + ":relic").get()));
    }

    private static CTag getCTag(ItemStack stack) {
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        CustomData dta = nms.get(DataComponents.CUSTOM_DATA);
        if (dta == null) dta = CustomData.EMPTY;

        CompoundTag tag = dta.copyTag();
        if (tag.getCompound("CustomData").isPresent()) {
            CompoundTag custom = tag.getCompound("CustomData").get();
            return new CTag(custom);
        } else {
            tag.put("CustomData", new CompoundTag());
            return new CTag(tag.getCompound("CustomData").get());
        }
    }
    private static void setCTag(CTag container, ItemStack stack) {
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        CustomData data = nms.get(DataComponents.CUSTOM_DATA);
        if (data == null) data = CustomData.EMPTY;
        CompoundTag tag = data.copyTag();
        tag.put("CustomData", container.toVanilla());
        data = CustomData.of(tag);
        nms.set(DataComponents.CUSTOM_DATA, data);
        ItemStack updated = CraftItemStack.asBukkitCopy(nms);
        stack.setItemMeta(updated.getItemMeta());
    }
}
