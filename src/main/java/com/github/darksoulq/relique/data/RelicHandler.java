package com.github.darksoulq.relique.data;

import com.github.darksoulq.abyssallib.common.config.Config;
import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.server.event.EventBus;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.world.data.attribute.Attribute;
import com.github.darksoulq.abyssallib.world.data.attribute.EntityAttributes;
import com.github.darksoulq.abyssallib.world.entity.CustomEntity;
import com.github.darksoulq.abyssallib.world.item.Item;
import com.github.darksoulq.relique.api.EntitySlotConfig;
import com.github.darksoulq.relique.api.RelicSlot;
import com.github.darksoulq.relique.component.RelicAttributeModifier;
import com.github.darksoulq.relique.component.RelicEquipSound;
import com.github.darksoulq.relique.component.RelicProperties;
import com.github.darksoulq.relique.core.RelicLoader;
import com.github.darksoulq.relique.core.RelicManager;
import com.github.darksoulq.relique.event.RelicEquipEvent;
import com.github.darksoulq.relique.event.RelicPreEquipEvent;
import com.github.darksoulq.relique.event.RelicTickEvent;
import com.github.darksoulq.relique.event.RelicUnequipEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RelicHandler {
    private static final Map<UUID, RelicHandler> CACHE = new ConcurrentHashMap<>();
    private static final Codec<Map<String, List<ItemStack>>> CODEC = Codec.map(Codecs.STRING, Codecs.ITEM_STACK.list());

    private final UUID entityId;
    private final Config config;
    private final Config.Value<Map<String, List<ItemStack>>> inventory;
    private boolean initializedSlots = false;

    private RelicHandler(UUID uuid) {
        this.entityId = uuid;
        this.config = new Config("relique", uuid.toString(), "entities");
        this.inventory = config.value("inventory", new HashMap<>(), CODEC);
    }

    public static RelicHandler get(LivingEntity entity) {
        RelicHandler handler = CACHE.computeIfAbsent(entity.getUniqueId(), RelicHandler::new);
        if (!handler.initializedSlots) {
            handler.applyEntityConfig(entity);
            handler.initializedSlots = true;
        }
        return handler;
    }

    public static void unload(UUID uuid) {
        CACHE.remove(uuid);
    }

    private Attribute getAttribute(String slotId) {
        Attribute attr = Registries.ATTRIBUTES.get("relique:" + slotId);
        if (attr == null && !slotId.contains(":")) {
            attr = Registries.ATTRIBUTES.get(slotId);
        }
        return attr;
    }

    private int getEnchantLevel(ItemStack item, String enchantKey) {
        if (item == null || item.isEmpty()) return 0;
        Enchantment ench = org.bukkit.Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantKey));
        return ench != null ? item.getEnchantmentLevel(ench) : 0;
    }

    private void applyEntityConfig(LivingEntity entity) {
        CustomEntity<?> ce = CustomEntity.resolve(entity);
        String typeId = ce != null ? ce.getId().asString() : entity.getType().getKey().asString();

        EntitySlotConfig conf = RelicLoader.ENTITY_CONFIGS.get(typeId);
        if (conf == null) {
            return;
        }

        EntityAttributes attrs = EntityAttributes.of(entityId);
        for (String slotId : conf.slots()) {
            Attribute attr = getAttribute(slotId);
            RelicSlot slot = RelicManager.getSlot(slotId);
            if (attr != null && slot != null) {
                if (!attrs.has(attr) || attrs.getBaseValue(attr) < slot.size()) {
                    attrs.setBaseValue(attr, slot.size());
                }
            }
        }
    }

    public void addSlotLimit(String slotId, int amount) {
        Attribute attr = getAttribute(slotId);
        if (attr == null) return;

        EntityAttributes attributes = EntityAttributes.of(entityId);
        RelicSlot slot = RelicManager.getSlot(slotId);
        int baseSize = slot != null ? slot.size() : (int) attr.defaultValue();

        if (!attributes.has(attr)) {
            attributes.setBaseValue(attr, baseSize);
        } else if (attributes.getBaseValue(attr) < baseSize) {
            attributes.setBaseValue(attr, baseSize);
        }

        attributes.setBaseValue(attr, attributes.getBaseValue(attr) + amount);
        save();
    }

    public void removeSlotLimit(String slotId, int amount) {
        Attribute attr = getAttribute(slotId);
        if (attr == null) return;

        EntityAttributes attributes = EntityAttributes.of(entityId);
        RelicSlot slot = RelicManager.getSlot(slotId);
        int baseSize = slot != null ? slot.size() : (int) attr.defaultValue();

        if (!attributes.has(attr)) {
            attributes.setBaseValue(attr, baseSize);
        } else if (attributes.getBaseValue(attr) < baseSize) {
            attributes.setBaseValue(attr, baseSize);
        }

        attributes.setBaseValue(attr, Math.max(0, attributes.getBaseValue(attr) - amount));
        save();
        validateInventoryBounds((LivingEntity) Bukkit.getEntity(entityId));
    }

    public void setSlotLimit(String slotId, int amount) {
        Attribute attr = getAttribute(slotId);
        if (attr == null) return;

        EntityAttributes attributes = EntityAttributes.of(entityId);
        attributes.setBaseValue(attr, Math.max(0, amount));
        save();
        validateInventoryBounds((LivingEntity) Bukkit.getEntity(entityId));
    }

    public List<ItemStack> getEquipped(String slotId) {
        return inventory.get().getOrDefault(slotId, new ArrayList<>());
    }

    public int getSlotLimit(String slotId) {
        Attribute attr = getAttribute(slotId);
        if (attr == null) return 0;

        EntityAttributes attributes = EntityAttributes.of(entityId);
        RelicSlot slot = RelicManager.getSlot(slotId);
        int baseSize = slot != null ? slot.size() : (int) attr.defaultValue();

        if (!attributes.has(attr)) {
            return baseSize;
        }

        if (attributes.getBaseValue(attr) < baseSize) {
            attributes.setBaseValue(attr, baseSize);
        }

        double limit = attributes.getValue(attr);
        return Math.max(0, (int) limit);
    }

    public boolean canUnequip(String slotId, int index, LivingEntity entity) {
        List<ItemStack> list = inventory.get().getOrDefault(slotId, new ArrayList<>());
        if (index >= list.size()) return true;

        ItemStack item = list.get(index);
        if (item == null || item.isEmpty()) return true;

        if (entity instanceof Player p && p.getGameMode() == org.bukkit.GameMode.CREATIVE) return true;

        return getEnchantLevel(item, "binding_curse") <= 0;
    }

    public boolean equip(String slotId, int index, ItemStack item) {
        LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);

        if (!RelicManager.isValid(slotId, item, entity)) return false;
        if (index >= getSlotLimit(slotId)) return false;
        if (entity != null && !canUnequip(slotId, index, entity)) return false;

        if (entity != null) {
            RelicPreEquipEvent preEvent = new RelicPreEquipEvent(entity, slotId, index, item);
            EventBus.post(preEvent);
            if (preEvent.isCancelled()) {
                return false;
            }
        }

        Map<String, List<ItemStack>> map = new HashMap<>(inventory.get());
        List<ItemStack> list = new ArrayList<>(map.getOrDefault(slotId, new ArrayList<>()));

        while (list.size() <= index) list.add(ItemStack.empty());

        ItemStack old = list.set(index, item.clone());
        map.put(slotId, list);
        inventory.set(map);

        if (old != null && !old.isEmpty()) {
            removeModifiers(old, slotId, index, entity);
            EventBus.post(new RelicUnequipEvent(entity, slotId, index, old));
        }
        if (!item.isEmpty()) {
            applyModifiers(item, slotId, index, entity);
            EventBus.post(new RelicEquipEvent(entity, slotId, index, item));

            if (entity != null) {
                Item itemObj = Item.resolve(item);
                if (itemObj == null) itemObj = new Item(item);
                if (itemObj.hasData(RelicEquipSound.TYPE)) {
                    Key sound = itemObj.getData(RelicEquipSound.TYPE).getValue().equip();
                    if (sound != null) {
                        entity.getWorld().playSound(entity.getLocation(), sound.asString(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }
            }
        }

        save();
        validateInventoryBounds(entity);
        return true;
    }

    public ItemStack unequip(String slotId, int index) {
        LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
        if (entity != null && !canUnequip(slotId, index, entity)) return ItemStack.empty();

        Map<String, List<ItemStack>> map = new HashMap<>(inventory.get());
        List<ItemStack> list = new ArrayList<>(map.getOrDefault(slotId, new ArrayList<>()));

        if (index >= list.size()) return ItemStack.empty();

        ItemStack removed = list.set(index, ItemStack.empty());
        map.put(slotId, list);
        inventory.set(map);

        if (removed != null && !removed.isEmpty()) {
            removeModifiers(removed, slotId, index, entity);
            EventBus.post(new RelicUnequipEvent(entity, slotId, index, removed));

            if (entity != null) {
                Item removedObj = Item.resolve(removed);
                if (removedObj == null) removedObj = new Item(removed);
                if (removedObj.hasData(RelicEquipSound.TYPE)) {
                    Key sound = removedObj.getData(RelicEquipSound.TYPE).getValue().unequip();
                    if (sound != null) {
                        entity.getWorld().playSound(entity.getLocation(), sound.asString(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }
            }

            save();
            validateInventoryBounds(entity);
        } else {
            save();
        }
        return removed;
    }

    public void syncModifiers() {
        LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
        if (entity == null) return;

        for (Map.Entry<String, List<ItemStack>> entry : inventory.get().entrySet()) {
            String slotId = entry.getKey();
            List<ItemStack> items = entry.getValue();
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (item != null && !item.isEmpty()) {
                    removeModifiers(item, slotId, i, entity);
                    applyModifiers(item, slotId, i, entity);
                }
            }
        }
    }

    public void updateItem(String slotId, int index, ItemStack item) {
        Map<String, List<ItemStack>> map = new HashMap<>(inventory.get());
        List<ItemStack> list = new ArrayList<>(map.getOrDefault(slotId, new ArrayList<>()));
        while (list.size() <= index) list.add(ItemStack.empty());
        list.set(index, item.clone());
        map.put(slotId, list);
        inventory.set(map);
        save();
    }

    public void damageItem(String slotId, int index, int amount) {
        List<ItemStack> list = inventory.get().getOrDefault(slotId, new ArrayList<>());
        if (index >= list.size()) return;
        ItemStack item = list.get(index);

        if (item == null || item.isEmpty()) return;

        Integer maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE);
        if (maxDamage == null || maxDamage <= 0) return;

        int unbreaking = getEnchantLevel(item, "unbreaking");
        int effectiveDamage = 0;
        for (int i = 0; i < amount; i++) {
            if (unbreaking == 0 || Math.random() < (1.0 / (unbreaking + 1))) {
                effectiveDamage++;
            }
        }

        if (effectiveDamage > 0) {
            int currentDamage = item.hasData(DataComponentTypes.DAMAGE) ? item.getData(DataComponentTypes.DAMAGE) : 0;
            int newDamage = currentDamage + effectiveDamage;
            item.setData(DataComponentTypes.DAMAGE, newDamage);

            if (newDamage >= maxDamage) {
                unequip(slotId, index);
                LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
                if (entity instanceof Player p) {
                    p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            } else {
                updateItem(slotId, index, item);
            }
        }
    }

    public void tick() {
        LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
        if (entity == null || !entity.isValid()) return;

        for (Map.Entry<String, List<ItemStack>> entry : inventory.get().entrySet()) {
            String slotId = entry.getKey();
            List<ItemStack> items = entry.getValue();
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (item != null && !item.isEmpty()) {
                    EventBus.post(new RelicTickEvent(entity, slotId, i, item));
                }
            }
        }
    }

    private void applyModifiers(ItemStack item, String slotId, int index, LivingEntity entity) {
        if (entity == null) return;
        Item i = Item.resolve(item);
        if (i == null) i = new Item(item);
        if (!i.hasData(RelicProperties.TYPE)) return;

        List<RelicAttributeModifier> mods = i.getData(RelicProperties.TYPE).getValue();
        EntityAttributes customAttributes = EntityAttributes.of(entityId);

        for (RelicAttributeModifier mod : mods) {
            if (mod.slots().isEmpty() || mod.slots().contains(slotId) || mod.slots().contains("any")) {
                String namespace = mod.id().namespace();
                String value = mod.id().value();
                String uniqueId = value + "_" + slotId + "_" + index;

                if (mod.attribute().namespace().equals("minecraft")) {
                    org.bukkit.attribute.Attribute bukkitAttr = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE).get(mod.attribute());
                    if (bukkitAttr != null) {
                        org.bukkit.attribute.AttributeInstance inst = entity.getAttribute(bukkitAttr);
                        if (inst != null) {
                            org.bukkit.attribute.AttributeModifier bukkitMod = new org.bukkit.attribute.AttributeModifier(
                                new NamespacedKey(namespace, uniqueId),
                                mod.amount(),
                                mod.operation(),
                                EquipmentSlotGroup.ANY
                            );
                            inst.removeModifier(bukkitMod.getKey());
                            inst.addModifier(bukkitMod);
                        }
                    }
                } else {
                    Attribute customAttr = Registries.ATTRIBUTES.get(mod.attribute().asString());
                    if (customAttr != null) {
                        com.github.darksoulq.abyssallib.world.data.attribute.AttributeModifier customMod =
                            new com.github.darksoulq.abyssallib.world.data.attribute.AttributeModifier(Key.key(namespace, uniqueId),
                                mod.amount(), mod.operation());
                        customAttributes.addModifier(customAttr, customMod);
                    }
                }
            }
        }
    }

    private void removeModifiers(ItemStack item, String slotId, int index, LivingEntity entity) {
        if (entity == null) return;
        Item i = Item.resolve(item);
        if (i == null) i = new Item(item);
        if (!i.hasData(RelicProperties.TYPE)) return;

        List<RelicAttributeModifier> mods = i.getData(RelicProperties.TYPE).getValue();
        EntityAttributes customAttributes = EntityAttributes.of(entityId);

        for (RelicAttributeModifier mod : mods) {
            if (mod.slots().isEmpty() || mod.slots().contains(slotId) || mod.slots().contains("any")) {
                String namespace = mod.id().namespace();
                String value = mod.id().value();
                String uniqueId = value + "_" + slotId + "_" + index;

                if (mod.attribute().namespace().equals("minecraft")) {
                    org.bukkit.attribute.Attribute bukkitAttr = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE).get(mod.attribute());
                    if (bukkitAttr != null) {
                        org.bukkit.attribute.AttributeInstance inst = entity.getAttribute(bukkitAttr);
                        if (inst != null) {
                            inst.removeModifier(new NamespacedKey(namespace, uniqueId));
                        }
                    }
                } else {
                    Attribute customAttr = Registries.ATTRIBUTES.get(mod.attribute().asString());
                    if (customAttr != null) {
                        customAttributes.removeModifier(customAttr, Key.key(namespace, uniqueId));
                    }
                }
            }
        }
    }

    private void validateInventoryBounds(LivingEntity entity) {
        Map<String, List<ItemStack>> map = new HashMap<>(inventory.get());
        boolean changed = false;

        for (String typeKey : map.keySet()) {
            int max = getSlotLimit(typeKey);
            List<ItemStack> items = map.get(typeKey);

            for (int i = items.size() - 1; i >= max; i--) {
                ItemStack overflow = items.set(i, ItemStack.empty());
                if (overflow != null && !overflow.isEmpty()) {
                    changed = true;
                    removeModifiers(overflow, typeKey, i, entity);
                    EventBus.post(new RelicUnequipEvent(entity, typeKey, i, overflow));
                    if (entity != null) {
                        entity.getWorld().dropItemNaturally(entity.getLocation(), overflow);
                    }
                }
            }
        }
        if (changed) {
            inventory.set(map);
            save();
        }
    }

    public void save() {
        config.save();
    }
}