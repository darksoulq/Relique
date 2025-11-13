package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.event.SubscribeEvent;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.api.Relic;
import com.github.darksoulq.relique.data.PlayerData;
import com.github.darksoulq.relique.api.RelicItem;
import com.github.darksoulq.relique.data.Slots;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class Events {
    @SubscribeEvent
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.STARTUP) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlayerData data = PlayerData.get(player);
                        data.all().forEach((key, value) -> value.forEach(item -> {
                            Optional<Slots.Slot> slot = Slots.get(key);
                            if (item == null) return;
                            if (slot.isEmpty()) return;
                            if (slot.get().getDisplay().isSimilar(item)) return;
                            Optional<Relic> relic = RelicItem.getRelic(item);
                            relic.ifPresent(rel -> rel.onTick(player, item));
                        }));
                    }
                }
            }.runTaskTimer(Relique.INSTANCE, 1L, 1L);
        }
    }
}
