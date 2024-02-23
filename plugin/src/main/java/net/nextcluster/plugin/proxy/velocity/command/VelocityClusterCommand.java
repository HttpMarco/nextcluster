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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.config.NextConfig;
import net.nextcluster.driver.resource.service.ClusterService;
import net.nextcluster.plugin.NextClusterPlugin;
import net.nextcluster.plugin.misc.IngameMessages;

public final class VelocityClusterCommand {

    private static final Command<CommandSource> SEND_HELP = context -> {
        final var player = (Player) context.getSource();
        sendMessage(player, NextClusterPlugin.instance().messages().value().getHelp());
        return Command.SINGLE_SUCCESS;
    };

    public static BrigadierCommand create(String name, NextConfig<IngameMessages> messages) {
        final var node = BrigadierCommand.literalArgumentBuilder(name)
            .executes(SEND_HELP)
            .then(
                BrigadierCommand.literalArgumentBuilder("service")
                    .executes(SEND_HELP)
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("service", StringArgumentType.word())
                            .executes(SEND_HELP)
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

}
