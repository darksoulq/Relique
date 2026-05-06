package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.ops.StringOps;
import com.github.darksoulq.abyssallib.common.util.Try;
import com.github.darksoulq.abyssallib.server.command.Command;
import com.github.darksoulq.abyssallib.server.command.DefaultConditions;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.world.item.component.ComponentMap;
import com.github.darksoulq.abyssallib.world.item.component.DataComponent;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.data.RelicHandler;
import com.github.darksoulq.relique.data.Resources;
import com.github.darksoulq.relique.gui.RelicGui;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InternalCommands {
    @Command(name = "relique")
    public void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.requires(DefaultConditions.hasPerm(PluginPermissions.OPEN_GUI))
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player p) {
                    RelicGui.open(p);
                }
                return Command.SUCCESS;
            })
            .then(Commands.literal("reload")
                .requires(DefaultConditions.hasPerm(PluginPermissions.RELOAD))
                .executes(ctx -> {
                    RelicLoader.clear();
                    RelicLoader.loadResource(Relique.INSTANCE, "");
                    RelicLoader.load();
                    Resources.setupAndRegister();
                    ctx.getSource().getSender().sendRichMessage("<green>Relique reloaded successfully.");
                    return Command.SUCCESS;
                })
            )
            .then(Commands.literal("slot")
                .requires(DefaultConditions.hasPerm(PluginPermissions.MODIFY_SLOTS))
                .then(Commands.literal("add")
                    .then(Commands.argument("target", ArgumentTypes.player())
                        .then(Commands.argument("slot", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                String remaining = builder.getRemainingLowerCase();
                                for (String slotId : RelicManager.getSlotsSorted()) {
                                    if (slotId.toLowerCase().startsWith(remaining)) {
                                        builder.suggest(slotId);
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                    Try<Player> targetGetter = Try.of(() -> targetResolver.resolve(ctx.getSource()).getFirst());
                                    if (!targetGetter.isSuccess()) {
                                        ctx.getSource().getSender().sendRichMessage("<red>Player not found.</red>");
                                        return Command.SUCCESS;
                                    }
                                    Player target = targetGetter.get();

                                    String slot = StringArgumentType.getString(ctx, "slot");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    RelicHandler.get(target).addSlotLimit(slot, amount);
                                    ctx.getSource().getSender().sendRichMessage("<green>Added " + amount + " to " + target.getName() + "'s " + slot + " slot(s).</green>");
                                    return Command.SUCCESS;
                                })
                            )
                        )
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("target", ArgumentTypes.player())
                        .then(Commands.argument("slot", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                String remaining = builder.getRemainingLowerCase();
                                for (String slotId : RelicManager.getSlotsSorted()) {
                                    if (slotId.toLowerCase().startsWith(remaining)) {
                                        builder.suggest(slotId);
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                    Try<Player> targetGetter = Try.of(() -> targetResolver.resolve(ctx.getSource()).getFirst());
                                    if (!targetGetter.isSuccess()) {
                                        ctx.getSource().getSender().sendRichMessage("<red>Player not found.</red>");
                                        return Command.SUCCESS;
                                    }
                                    Player target = targetGetter.get();

                                    String slot = StringArgumentType.getString(ctx, "slot");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    RelicHandler.get(target).removeSlotLimit(slot, amount);
                                    ctx.getSource().getSender().sendRichMessage("<green>Removed " + amount + " from " + target.getName() + "'s " + slot + " slot(s).</green>");
                                    return Command.SUCCESS;
                                })
                            )
                        )
                    )
                )
                .then(Commands.literal("set")
                    .then(Commands.argument("target", ArgumentTypes.player())
                        .then(Commands.argument("slot", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                String remaining = builder.getRemainingLowerCase();
                                for (String slotId : RelicManager.getSlotsSorted()) {
                                    if (slotId.toLowerCase().startsWith(remaining)) {
                                        builder.suggest(slotId);
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                    Try<Player> targetGetter = Try.of(() -> targetResolver.resolve(ctx.getSource()).getFirst());
                                    if (!targetGetter.isSuccess()) {
                                        ctx.getSource().getSender().sendRichMessage("<red>Player not found.</red>");
                                        return Command.SUCCESS;
                                    }
                                    Player target = targetGetter.get();

                                    String slot = StringArgumentType.getString(ctx, "slot");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    RelicHandler.get(target).setSlotLimit(slot, amount);
                                    ctx.getSource().getSender().sendRichMessage("<green>Set " + target.getName() + "'s " + slot + " slot(s) to " + amount + ".</green>");
                                    return Command.SUCCESS;
                                })
                            )
                        )
                    )
                )
            ).then(Commands.literal("print")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return Command.FAILURE;
                    ItemStack mainHand = player.getEquipment().getItemInMainHand();
                    if (mainHand.isEmpty()) return Command.FAILURE;
                    ComponentMap map = new ComponentMap(mainHand);
                    List<String> ids = map.getAllComponents().stream().map(comp -> Registries.DATA_COMPONENT_TYPES.getId(comp.getType())).toList();
                    player.sendRichMessage(ids.toString());
                    return Command.SUCCESS;
                })
            );
    }
}