package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.translation.ClientItemModifier;
import com.github.darksoulq.abyssallib.world.item.component.ComponentMap;
import com.github.darksoulq.relique.component.RelicAttributeModifier;
import com.github.darksoulq.relique.component.RelicProperties;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RelicTooltipModifier implements ClientItemModifier {

    @Override
    public boolean modify(ItemStack item, Player player) {
        if (item == null || item.isEmpty()) return false;

        ComponentMap map = new ComponentMap(item);

        List<String> validSlots = new ArrayList<>();
        for (String s : RelicManager.getSlotsSorted()) {
            if (RelicManager.isValid(s, item, player)) {
                validSlots.add(s);
            }
        }

        boolean hasRelicProps = map.hasData(RelicProperties.TYPE);

        if (validSlots.isEmpty() && !hasRelicProps) return false;

        List<Component> newLore = new ArrayList<>();

        if (!validSlots.isEmpty()) {
            Component slotText = Component.empty().append(Component.text("Slot: ").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            for (int i = 0; i < validSlots.size(); i++) {
                if (i > 0) slotText = slotText.append(Component.text(", ").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                slotText = slotText.append(Component.translatable("relique.slot." + validSlots.get(i)).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            }
            newLore.add(slotText);
        }

        if (item.hasData(DataComponentTypes.LORE)) {
            ItemLore existingLore = item.getData(DataComponentTypes.LORE);
            if (existingLore != null) {
                newLore.addAll(existingLore.lines());
            }
        }

        if (hasRelicProps) {
            RelicProperties props = map.getData(RelicProperties.TYPE);
            if (props != null && !props.getValue().attributes().isEmpty()) {
                newLore.add(Component.empty());

                Map<String, List<RelicAttributeModifier>> bySlot = new LinkedHashMap<>();
                for (RelicAttributeModifier mod : props.getValue().attributes()) {
                    if (mod.slots().isEmpty()) {
                        bySlot.computeIfAbsent("any", k -> new ArrayList<>()).add(mod);
                    } else {
                        for (String s : mod.slots()) {
                            bySlot.computeIfAbsent(s, k -> new ArrayList<>()).add(mod);
                        }
                    }
                }

                for (Map.Entry<String, List<RelicAttributeModifier>> entry : bySlot.entrySet()) {
                    String slot = entry.getKey();
                    Component header;
                    if (slot.equals("any")) {
                        header = Component.text("When Equipped:").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
                    } else {
                        header = Component.text("When on ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                            .append(Component.translatable("relique.slot." + slot).decoration(TextDecoration.ITALIC, false))
                            .append(Component.text(":").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                    }
                    newLore.add(header);

                    for (RelicAttributeModifier mod : entry.getValue()) {
                        double amt = mod.amount();
                        boolean isPercent = mod.operation() == AttributeModifier.Operation.ADD_SCALAR || mod.operation() == AttributeModifier.Operation.MULTIPLY_SCALAR_1;
                        if (isPercent) amt *= 100;

                        String amtStr = (amt > 0 ? "+" : "") + String.format(Locale.US, "%.2f", amt).replace(".00", "");
                        if (isPercent) amtStr += "%";

                        NamedTextColor color = amt > 0 ? NamedTextColor.BLUE : NamedTextColor.RED;

                        String translationKey;
                        if (mod.attribute().namespace().equals("minecraft")) {
                            translationKey = "attribute.name." + mod.attribute().value();
                        } else {
                            translationKey = "attribute.name." + mod.attribute().namespace() + "." + mod.attribute().value();
                        }

                        Component attrComp = Component.empty()
                            .append(Component.text(" " + amtStr + " ").color(color).decoration(TextDecoration.ITALIC, false))
                            .append(Component.translatable(translationKey).color(color).decoration(TextDecoration.ITALIC, false));

                        newLore.add(attrComp);
                    }
                }
            }
        }

        item.setData(DataComponentTypes.LORE, ItemLore.lore(newLore));
        return true;
    }
}