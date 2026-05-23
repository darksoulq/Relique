package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.common.util.Try;
import com.github.darksoulq.abyssallib.server.command.BaseCommand;
import com.github.darksoulq.abyssallib.server.command.CommandResult;
import com.github.darksoulq.abyssallib.server.command.DefaultConditions;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.world.item.component.ComponentMap;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.data.RelicHandler;
import com.github.darksoulq.relique.data.Resources;
import com.github.darksoulq.relique.gui.RelicGui;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InternalCommands extends BaseCommand {

    public InternalCommands() {
        super("relique");
        setRequirement(DefaultConditions.hasPerm(PluginPermissions.OPEN_GUI));

        setDefaultExecutor(ctx -> {
            if (ctx.getSource().getSender() instanceof Player p) {
                RelicGui.open(p);
            }
            return CommandResult.success();
        });

        LiteralArgumentBuilder<CommandSourceStack> reloadLit = Commands.literal("reload").requires(DefaultConditions.hasPerm(PluginPermissions.RELOAD));

        LiteralArgumentBuilder<CommandSourceStack> slotLit = Commands.literal("slot").requires(DefaultConditions.hasPerm(PluginPermissions.MODIFY_SLOTS));
        LiteralArgumentBuilder<CommandSourceStack> addLit = Commands.literal("add");
        LiteralArgumentBuilder<CommandSourceStack> removeLit = Commands.literal("remove");
        LiteralArgumentBuilder<CommandSourceStack> setLit = Commands.literal("set");

        RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> targetArg = Commands.argument("target", ArgumentTypes.player());
        RequiredArgumentBuilder<CommandSourceStack, String> slotArg = Commands.argument("slot", StringArgumentType.word()).suggests((ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            for (String slotId : RelicManager.getSlotsSorted()) {
                if (slotId.toLowerCase().startsWith(remaining)) {
                    builder.suggest(slotId);
                }
            }
            return builder.buildFuture();
        });
        RequiredArgumentBuilder<CommandSourceStack, Integer> amountArg = Commands.argument("amount", IntegerArgumentType.integer());

        addSyntax(ctx -> {
            RelicLoader.clear();
            RelicLoader.loadResource(Relique.INSTANCE, "");
            RelicLoader.load();
            Resources.setupAndRegister();
            ctx.getSource().getSender().sendRichMessage("<green>Relique reloaded successfully.</green>");
            return CommandResult.success();
        }, reloadLit);

        addSyntax(ctx -> {
            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            Try<Player> targetGetter = Try.of(() -> targetResolver.resolve(ctx.getSource()).getFirst());
            if (!targetGetter.isSuccess()) {
                ctx.getSource().getSender().sendRichMessage("<red>Player not found.</red>");
                return CommandResult.failure();
            }
            Player target = targetGetter.get();

            String slot = StringArgumentType.getString(ctx, "slot");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            RelicHandler.get(target).addSlotLimit(slot, amount);
            ctx.getSource().getSender().sendRichMessage("<green>Added " + amount + " to " + target.getName() + "'s " + slot + " slot(s).</green>");
            return CommandResult.success();
        }, slotLit, addLit, targetArg, slotArg, amountArg);

        addSyntax(ctx -> {
            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            Try<Player> targetGetter = Try.of(() -> targetResolver.resolve(ctx.getSource()).getFirst());
            if (!targetGetter.isSuccess()) {
                ctx.getSource().getSender().sendRichMessage("<red>Player not found.</red>");
                return CommandResult.failure();
            }
            Player target = targetGetter.get();

            String slot = StringArgumentType.getString(ctx, "slot");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            RelicHandler.get(target).removeSlotLimit(slot, amount);
            ctx.getSource().getSender().sendRichMessage("<green>Removed " + amount + " from " + target.getName() + "'s " + slot + " slot(s).</green>");
            return CommandResult.success();
        }, slotLit, removeLit, targetArg, slotArg, amountArg);

        addSyntax(ctx -> {
            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            Try<Player> targetGetter = Try.of(() -> targetResolver.resolve(ctx.getSource()).getFirst());
            if (!targetGetter.isSuccess()) {
                ctx.getSource().getSender().sendRichMessage("<red>Player not found.</red>");
                return CommandResult.failure();
            }
            Player target = targetGetter.get();

            String slot = StringArgumentType.getString(ctx, "slot");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            RelicHandler.get(target).setSlotLimit(slot, amount);
            ctx.getSource().getSender().sendRichMessage("<green>Set " + target.getName() + "'s " + slot + " slot(s) to " + amount + ".</green>");
            return CommandResult.success();
        }, slotLit, setLit, targetArg, slotArg, amountArg);
    }
}