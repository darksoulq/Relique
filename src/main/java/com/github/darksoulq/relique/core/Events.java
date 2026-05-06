package com.github.darksoulq.relique.core;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.github.darksoulq.abyssallib.server.event.EventBus;
import com.github.darksoulq.abyssallib.server.event.SubscribeEvent;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.api.DropRule;
import com.github.darksoulq.relique.data.RelicHandler;
import com.github.darksoulq.relique.event.RelicDropEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Levelled;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Events {

    private int getEnchantLevel(ItemStack item, String enchantKey) {
        if (item == null || item.isEmpty()) return 0;
        Enchantment ench = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(enchantKey));
        return ench != null ? item.getEnchantmentLevel(ench) : 0;
    }

    private int getTotalEnchantLevel(LivingEntity entity, String enchantKey) {
        int total = 0;
        RelicHandler handler = RelicHandler.get(entity);
        for (String slotId : RelicManager.getSlotsSorted()) {
            for (ItemStack item : handler.getEquipped(slotId)) {
                total += getEnchantLevel(item, enchantKey);
            }
        }
        return total;
    }

    private boolean isUndead(LivingEntity entity) {
        return switch (entity.getType().name()) {
            case "ZOMBIE", "SKELETON", "WITHER_SKELETON", "WITHER", "ZOMBIE_VILLAGER",
                 "PHANTOM", "DROWNED", "HUSK", "STRAY", "ZOMBIFIED_PIGLIN",
                 "ZOGLIN", "SKELETON_HORSE", "ZOMBIE_HORSE", "BOGGED" -> true;
            default -> false;
        };
    }

    private boolean isArthropod(LivingEntity entity) {
        return switch (entity.getType().name()) {
            case "SPIDER", "CAVE_SPIDER", "BEE", "SILVERFISH", "ENDERMITE" -> true;
            default -> false;
        };
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        RelicHandler handler = RelicHandler.get(event.getPlayer());
        handler.syncModifiers();
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerQuitEvent event) {
        RelicHandler data = RelicHandler.get(event.getPlayer());
        data.save();
        RelicHandler.unload(event.getPlayer().getUniqueId());
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            RelicHandler.get(living);
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            RelicHandler.get(player).tick();
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null || item.isEmpty()) return;

        if (item.hasData(DataComponentTypes.EQUIPPABLE)) return;

        Player p = event.getPlayer();

        if (event.hasBlock() && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType().isInteractable() && !p.isSneaking()) {
                return;
            }
        }

        RelicHandler handler = RelicHandler.get(p);

        for (String slotId : RelicManager.getSlotsSorted()) {
            if (RelicManager.isValid(slotId, item, p)) {
                int limit = handler.getSlotLimit(slotId);
                for (int i = 0; i < limit; i++) {
                    List<ItemStack> equippedList = handler.getEquipped(slotId);
                    ItemStack equipped = equippedList.size() > i ? equippedList.get(i) : null;

                    if (equipped == null || equipped.isEmpty() || equipped.getType() == Material.AIR) {
                        ItemStack toEquip = item.clone();
                        toEquip.setAmount(1);
                        if (handler.equip(slotId, i, toEquip)) {
                            item.setAmount(item.getAmount() - 1);
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(EntityDeathEvent event) {
        RelicHandler data = RelicHandler.get(event.getEntity());

        boolean keepInventory = false;
        if (event.getEntity() instanceof Player p) {
            keepInventory = p.getWorld().getGameRuleValue(GameRules.KEEP_INVENTORY);
        }

        for (String slotId : RelicManager.getSlotsSorted()) {
            List<ItemStack> items = data.getEquipped(slotId);
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (item == null || item.isEmpty()) continue;

                if (!keepInventory && getEnchantLevel(item, "vanishing_curse") > 0) {
                    data.unequip(slotId, i);
                    continue;
                }

                DropRule rule = RelicManager.getSlot(slotId).dropRule();
                if (rule == DropRule.ALWAYS_KEEP || (rule == DropRule.DEFAULT && keepInventory)) {
                    continue;
                }

                if (!data.canUnequip(slotId, i, event.getEntity())) {
                    continue;
                }

                data.unequip(slotId, i);

                if (rule == DropRule.ALWAYS_DROP || rule == DropRule.DEFAULT) {
                    RelicDropEvent dropEvent = new RelicDropEvent(event.getEntity(), slotId, i, item);
                    EventBus.post(dropEvent);
                    event.getDrops().add(dropEvent.getItem());
                }
            }
        }
    }

    @SubscribeEvent
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        RelicHandler handler = RelicHandler.get(player);
        int xp = event.getAmount();
        if (xp <= 0) return;

        List<String> mSlots = new ArrayList<>();
        List<Integer> mIndices = new ArrayList<>();
        List<ItemStack> mItems = new ArrayList<>();

        for (String slotId : RelicManager.getSlotsSorted()) {
            List<ItemStack> equipped = handler.getEquipped(slotId);
            for (int i = 0; i < equipped.size(); i++) {
                ItemStack item = equipped.get(i);
                if (item != null && !item.isEmpty() && getEnchantLevel(item, "mending") > 0) {
                    Integer maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE);
                    if (maxDamage != null && maxDamage > 0) {
                        int currentDamage = item.hasData(DataComponentTypes.DAMAGE) ? item.getData(DataComponentTypes.DAMAGE) : 0;
                        if (currentDamage > 0) {
                            mSlots.add(slotId);
                            mIndices.add(i);
                            mItems.add(item);
                        }
                    }
                }
            }
        }

        if (!mItems.isEmpty()) {
            int random = ThreadLocalRandom.current().nextInt(mItems.size());
            ItemStack target = mItems.get(random);

            int currentDamage = target.hasData(DataComponentTypes.DAMAGE) ? target.getData(DataComponentTypes.DAMAGE) : 0;
            int repairAmount = Math.min(xp * 2, currentDamage);

            target.setData(DataComponentTypes.DAMAGE, currentDamage - repairAmount);
            handler.updateItem(mSlots.get(random), mIndices.get(random), target);

            xp -= (int) Math.ceil(repairAmount / 2.0);
            event.setAmount(xp);
        }
    }

    @SubscribeEvent
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        RelicHandler handler = RelicHandler.get(entity);
        int epf = 0;

        for (String slotId : RelicManager.getSlotsSorted()) {
            List<ItemStack> equipped = handler.getEquipped(slotId);
            for (int i = 0; i < equipped.size(); i++) {
                ItemStack item = equipped.get(i);
                if (item == null || item.isEmpty()) continue;

                epf += getEnchantLevel(item, "protection");

                switch (event.getCause()) {
                    case FIRE, FIRE_TICK, LAVA, HOT_FLOOR -> epf += getEnchantLevel(item, "fire_protection") * 2;
                    case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> epf += getEnchantLevel(item, "blast_protection") * 2;
                    case PROJECTILE -> epf += getEnchantLevel(item, "projectile_protection") * 2;
                    case FALL -> epf += getEnchantLevel(item, "feather_falling") * 3;
                }

                if (event.getDamage() > 0.5 &&
                    (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)) {
                    if (Math.random() < 0.25) {
                        handler.damageItem(slotId, i, 1);
                    }
                }
            }
        }

        if (epf > 0) {
            epf = Math.min(20, epf);
            double reduction = epf * 0.04;
            event.setDamage(event.getDamage() * (1.0 - reduction));
        }
    }

    @SubscribeEvent
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        int thorns = getTotalEnchantLevel(entity, "thorns");
        if (thorns > 0 && event.getDamager() instanceof LivingEntity attacker) {
            if (Math.random() < thorns * 0.15) {
                int damage = 1 + ThreadLocalRandom.current().nextInt(4);
                if (thorns > 10) damage += thorns - 10;
                attacker.damage(damage, entity);
            }
        }

        if (event.getDamager() instanceof LivingEntity damager) {
            int sharpness = getTotalEnchantLevel(damager, "sharpness");
            int smite = getTotalEnchantLevel(damager, "smite");
            int bane = getTotalEnchantLevel(damager, "bane_of_arthropods");
            int fireAspect = getTotalEnchantLevel(damager, "fire_aspect");
            int knockback = getTotalEnchantLevel(damager, "knockback");

            double extraDamage = 0;

            if (sharpness > 0) {
                extraDamage += 0.5 * sharpness + 0.5;
            }

            if (smite > 0 && isUndead(entity)) {
                extraDamage += 2.5 * smite;
            }

            if (bane > 0 && isArthropod(entity)) {
                extraDamage += 2.5 * bane;
                int duration = 20 + ThreadLocalRandom.current().nextInt(10 * bane);
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 3));
            }

            if (extraDamage > 0) {
                event.setDamage(event.getDamage() + extraDamage);
            }

            if (fireAspect > 0) {
                int ticks = fireAspect * 80;
                entity.setFireTicks(Math.max(entity.getFireTicks(), ticks));
            }

            if (knockback > 0) {
                Vector direction = damager.getLocation().getDirection().setY(0).normalize();
                entity.setVelocity(entity.getVelocity().add(direction.multiply(knockback * 0.5)));
            }
        } else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof LivingEntity shooter) {
            int power = getTotalEnchantLevel(shooter, "power");
            int flame = getTotalEnchantLevel(shooter, "flame");

            if (power > 0) {
                event.setDamage(event.getDamage() + (event.getDamage() * 0.25 * (power + 1)));
            }
            if (flame > 0) {
                entity.setFireTicks(Math.max(entity.getFireTicks(), 100));
            }
        }
    }

    @SubscribeEvent
    public void onAirChange(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (event.getAmount() >= entity.getRemainingAir()) return;

        int respiration = getTotalEnchantLevel(entity, "respiration");
        if (respiration > 0) {
            double chance = respiration / (double) (respiration + 1);
            if (Math.random() < chance) {
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.WATER || player.isInWater()) {
            int aquaAffinity = getTotalEnchantLevel(player, "aqua_affinity");
            if (aquaAffinity > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, false, false, false));
            }
        }
    }

    @SubscribeEvent
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        Player player = event.getPlayer();

        int soulSpeed = getTotalEnchantLevel(player, "soul_speed");
        if (soulSpeed > 0 && player.isOnGround()) {
            Block blockUnder = player.getLocation().subtract(0, 0.1, 0).getBlock();
            if (blockUnder.getType() == Material.SOUL_SAND || blockUnder.getType() == Material.SOUL_SOIL) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, soulSpeed - 1, false, false, false));
            }
        }

        int depthStrider = getTotalEnchantLevel(player, "depth_strider");
        if (depthStrider > 0 && player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, depthStrider - 1, false, false, false));
        }

        int swiftSneak = getTotalEnchantLevel(player, "swift_sneak");
        if (swiftSneak > 0 && player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, swiftSneak - 1, false, false, false));
        }

        if (player.getVehicle() != null || !player.isOnGround()) return;

        int frostWalker = getTotalEnchantLevel(player, "frost_walker");
        if (frostWalker > 0) {
            Block start = player.getLocation().getBlock();
            int radius = Math.min(16, 2 + frostWalker);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        Block b = start.getRelative(x, -1, z);
                        if (b.getType() == Material.WATER) {
                            Block above = b.getRelative(0, 1, 0);
                            if (above.getType() == Material.AIR) {
                                Levelled level = (Levelled) b.getBlockData();
                                if (level.getLevel() == 0) {
                                    BlockState state = b.getState();
                                    state.setType(Material.FROSTED_ICE);
                                    Ageable ageable = (Ageable) state.getBlockData();
                                    ageable.setAge(0);
                                    state.setBlockData(ageable);

                                    EntityBlockFormEvent formEvent = new EntityBlockFormEvent(player, b, state);
                                    Bukkit.getPluginManager().callEvent(formEvent);
                                    if (!formEvent.isCancelled()) {
                                        formEvent.getNewState().update(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}