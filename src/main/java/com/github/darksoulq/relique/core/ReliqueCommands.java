package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.command.Command;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.api.Relic;
import com.github.darksoulq.relique.api.RelicItem;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReliqueCommands {
    @Command(name = "relique")
    public void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.executes(ctx -> {
            Entity entity = ctx.getSource().getExecutor();
            if (!(entity instanceof Player player)) return Command.SUCCESS;
            RelicGui.open(player);
            return Command.SUCCESS;
        });
    }
}
