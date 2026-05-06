package com.github.darksoulq.relique.api;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface RelicValidator {
    boolean isValid(String slotId, ItemStack item, LivingEntity entity);
}