/*
 * MIT License
 *
 * Copyright (c) 2024 nextCluster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.nextcluster.plugin.proxy.velocity.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.config.NextConfig;
import net.nextcluster.driver.resource.group.ClusterGroup;
import net.nextcluster.driver.resource.service.ClusterService;
import net.nextcluster.plugin.ClusterCommand;
import net.nextcluster.plugin.NextClusterPlugin;
import net.nextcluster.plugin.misc.IngameMessages;

public final class VelocityClusterCommand implements ClusterCommand {

    private static final Command<CommandSource> SEND_HELP = context -> {
        final var player = (Player) context.getSource();
        sendMessage(player, NextClusterPlugin.instance().messages().value().getHelp());
        return Command.SINGLE_SUCCESS;
    };

    public static BrigadierCommand create(String name, NextConfig<IngameMessages> messages) {
        final var node = BrigadierCommand.literalArgumentBuilder(name)
            .requires(source -> source.hasPermission(PERMISSION))
            .executes(SEND_HELP)
            .then(
                BrigadierCommand.literalArgumentBuilder("service")
                    .executes(SEND_HELP)
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("service", StringArgumentType.word())
                            .executes(context -> {
                                final var service = context.getArgument("service", String.class);
                                NextCluster.instance().serviceProvider().service(service).ifPresentOrElse(clusterService -> {
                                    printServiceInformation(context.getSource(), clusterService, messages.value());
                                }, () -> sendMessage(
                                    context.getSource(),
                                    messages.value().getServiceNotFound().replace("%service%", service)
                                ));
                                return Command.SINGLE_SUCCESS;
                            })
                            .suggests((context, builder) -> {
                                for (ClusterService service : NextCluster.instance().serviceProvider().services()) {
                                    builder.suggest(service.name());
                                }
                                return builder.buildFuture();
                            })
                            .then(
                                BrigadierCommand.literalArgumentBuilder("stop")
                                    .executes(context -> {
                                        final var service = context.getArgument("service", String.class);
                                        NextCluster.instance().serviceProvider().service(service).ifPresentOrElse(clusterService -> {
                                            clusterService.shutdown();
                                            sendMessage(
                                                context.getSource(),
                                                messages.value().getServiceStopped().replace("%service%", service)
                                            );
                                        }, () -> sendMessage(
                                            context.getSource(),
                                            messages.value().getServiceNotFound().replace("%service%", service)
                                        ));
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .build()
                            )
                            .then(
                                BrigadierCommand.literalArgumentBuilder("execute")
                                    .executes(SEND_HELP)
                                    .then(
                                        BrigadierCommand.requiredArgumentBuilder("command", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                final var service = context.getArgument("service", String.class);
                                                final var command = context.getArgument("command", String.class);
                                                NextCluster.instance().serviceProvider().service(service).ifPresentOrElse(clusterService -> {
                                                    clusterService.execute(command);
                                                    sendMessage(
                                                        context.getSource(),
                                                        messages.value().getCommandExecutedOnService()
                                                            .replace("%service%", clusterService.name())
                                                            .replace("%command%", command)
                                                    );
                                                }, () -> sendMessage(
                                                    context.getSource(),
                                                    messages.value().getServiceNotFound().replace("%service%", service)
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
            )
            .then(
                BrigadierCommand.literalArgumentBuilder("services")
                    .executes(context -> {
                        NextCluster.instance().serviceProvider().servicesAsync().thenAccept(services -> {
                            sendMessage(
                                context.getSource(),
                                messages.value().getPrefix() + " <white>Services: <gray>" + services.size()
                            );
                            for (ClusterService service : services) {
                                printServiceInformation(context.getSource(), service, messages.value());
                            }
                        });
                        return Command.SINGLE_SUCCESS;
                    })
                    .build()
            )
            .then(
                BrigadierCommand.literalArgumentBuilder("group")
                    .executes(SEND_HELP)
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("group", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                for (ClusterGroup group : NextCluster.instance().groupProvider().groups()) {
                                    builder.suggest(group.name());
                                }
                                return builder.buildFuture();
                            })
                            .then(
                                BrigadierCommand.literalArgumentBuilder("maintenance")
                                    .executes(SEND_HELP)
                                    .then(
                                        BrigadierCommand.requiredArgumentBuilder("state", BoolArgumentType.bool())
                                            .suggests((context, builder) -> {
                                                builder.suggest("true");
                                                builder.suggest("false");
                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                final var state = context.getArgument("state", Boolean.class);
                                                final var group = context.getArgument("group", String.class);

                                                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(clusterGroup -> {
                                                    clusterGroup.asBuilder().withMaintenance(state).publish();
                                                    sendMessage(
                                                        context.getSource(),
                                                        messages.value().getGroupUpdated().replace("%group%", group)
                                                    );
                                                }, () -> sendMessage(
                                                    context.getSource(),
                                                    messages.value().getGroupNotFound().replace("%group%", group)
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .build()
                                    )
                                    .build()
                            )
                            .then(
                                BrigadierCommand.literalArgumentBuilder("fallback")
                                    .executes(SEND_HELP)
                                    .then(
                                        BrigadierCommand.requiredArgumentBuilder("state", BoolArgumentType.bool())
                                            .suggests((context, builder) -> {
                                                builder.suggest("true");
                                                builder.suggest("false");
                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                final var state = context.getArgument("state", Boolean.class);
                                                final var group = context.getArgument("group", String.class);

                                                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(clusterGroup -> {
                                                    clusterGroup.asBuilder().withFallback(state).publish();
                                                    sendMessage(
                                                        context.getSource(),
                                                        messages.value().getGroupUpdated().replace("%group%", group)
                                                    );
                                                }, () -> sendMessage(
                                                    context.getSource(),
                                                    messages.value().getGroupNotFound().replace("%group%", group)
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .build()
                                    )
                                    .build()
                            )
                            .then(
                                BrigadierCommand.literalArgumentBuilder("minServers")
                                    .executes(SEND_HELP)
                                    .then(
                                        BrigadierCommand.requiredArgumentBuilder("amount", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                final var amount = context.getArgument("amount", Integer.class);
                                                final var group = context.getArgument("group", String.class);
                                                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(clusterGroup -> {
                                                    clusterGroup.asBuilder().withMinOnline(amount).publish();
                                                    sendMessage(
                                                        context.getSource(),
                                                        messages.value().getGroupUpdated().replace("%group%", group)
                                                    );
                                                }, () -> sendMessage(
                                                    context.getSource(),
                                                    messages.value().getGroupNotFound().replace("%group%", group)
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .build()
                                    )
                                    .build()
                            )
                            .then(
                                BrigadierCommand.literalArgumentBuilder("maxServers")
                                    .executes(SEND_HELP)
                                    .then(
                                        BrigadierCommand.requiredArgumentBuilder("amount", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                final var amount = context.getArgument("amount", Integer.class);
                                                final var group = context.getArgument("group", String.class);
                                                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(clusterGroup -> {
                                                    clusterGroup.asBuilder().withMaxOnline(amount).publish();
                                                    sendMessage(
                                                        context.getSource(),
                                                        messages.value().getGroupUpdated().replace("%group%", group)
                                                    );
                                                }, () -> sendMessage(
                                                    context.getSource(),
                                                    messages.value().getGroupNotFound().replace("%group%", group)
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();

        return new BrigadierCommand(node);
    }

    private static void sendMessage(CommandSource player, String message) {
        if (!NextClusterPlugin.instance().messages().value().isMinimessage()) {
            player.sendMessage(Component.text(message));
            return;
        }
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    private static void printServiceInformation(CommandSource player, ClusterService service, IngameMessages messages) {
        final var prefix = messages.getPrefix();

        sendMessage(player, prefix + " <dark_gray>- <white>" + service.name());
        final var information = service.information();
        if (information == null) {
            sendMessage(player, prefix + " <dark_gray>   » <red><i>No information available");
            return;
        }
        sendMessage(
            player, prefix + " <dark_gray>   » <gray>Players: <white>" +
                information.getOnlinePlayers() + "<dark_gray>/<white>" + information.getMaxPlayers()
        );
        sendMessage(
            player, prefix + " <dark_gray>   » <gray>MOTD: <white>" + information.getMotd()
        );
        sendMessage(
            player, prefix + " <dark_gray>   » <gray>Platform: <white>" + information.getPlatform().name()
        );
    }

}
