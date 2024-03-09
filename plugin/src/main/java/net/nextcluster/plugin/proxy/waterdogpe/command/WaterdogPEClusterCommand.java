package net.nextcluster.plugin.proxy.waterdogpe.command;

import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.config.NextConfig;
import net.nextcluster.driver.resource.service.ClusterService;
import net.nextcluster.plugin.ClusterCommand;
import net.nextcluster.plugin.NextClusterPlugin;
import net.nextcluster.plugin.misc.IngameMessages;

import java.util.Arrays;

public final class WaterdogPEClusterCommand extends Command implements ClusterCommand {

    private final NextConfig<IngameMessages> messages;
    public WaterdogPEClusterCommand(NextConfig<IngameMessages> messages) {
        super("cluster");
        this.messages = messages;
    }


    @Override
    public boolean onExecute(CommandSender sender, String s, String[] args) {
        var player = (ProxiedPlayer) sender;

        if (!sender.hasPermission(PERMISSION)) {
            return false;
        }

        if (args[0].equalsIgnoreCase("service")) {
            var service = args[1];
            if (args[2].equalsIgnoreCase("service")) {
                NextCluster.instance().serviceProvider().service(service).ifPresentOrElse(clusterService -> {
                    clusterService.shutdown();
                    sendMessage(player,
                        messages.value().getServiceStopped().replace("%service%", service)
                    );
                }, () -> sendMessage(
                    player,
                    messages.value().getServiceNotFound().replace("%service%", service)
                ));
            } else if (args[2].equalsIgnoreCase("execute")) {
                final var command = String.join(" ", Arrays.stream(args).toList().subList(2, args.length));
                NextCluster.instance().serviceProvider().service(service).ifPresentOrElse(clusterService -> {
                    clusterService.execute(command);
                    sendMessage(player,
                        messages.value().getCommandExecutedOnService()
                            .replace("%service%", clusterService.name())
                            .replace("%command%", command)
                    );
                }, () -> sendMessage(
                    player,
                    messages.value().getServiceNotFound().replace("%service%", service)
                ));
            }
        }

        if (args[0].equalsIgnoreCase("services")) {

            NextCluster.instance().serviceProvider().servicesAsync().thenAccept(it -> {
                sendMessage(player,
                    messages.value().getPrefix() + " <white>Services: <gray>" + it.size()
                );
                for (ClusterService service : it) {
                    printServiceInformation(player, service, messages.value());
                }
            });
        }

        if (args[0].equalsIgnoreCase("group")) {
            var group = args[1];

            if (args[2].equalsIgnoreCase("maintenance")) {
                var state = Boolean.parseBoolean(args[3]);
                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(it -> {
                    it.asBuilder().withMaintenance(state).publish();
                    sendMessage(
                        player,
                        messages.value().getGroupUpdated().replace("%group%", group)
                    );
                }, () -> sendMessage(
                    player,
                    messages.value().getGroupNotFound().replace("%group%", group)
                ));
            } else if (args[2].equalsIgnoreCase("fallback")) {
                var state = Boolean.parseBoolean(args[3]);
                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(it -> {
                    it.asBuilder().withFallback(state).publish();
                    sendMessage(
                        player,
                        messages.value().getGroupUpdated().replace("%group%", group)
                    );
                }, () -> sendMessage(
                    player,
                    messages.value().getGroupNotFound().replace("%group%", group)
                ));
            } else if (args[2].equalsIgnoreCase("minServers")) {
                var state = Integer.parseInt(args[3]);
                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(it -> {
                    it.asBuilder().withMinOnline(state).publish();
                    sendMessage(
                        player,
                        messages.value().getGroupUpdated().replace("%group%", group)
                    );
                }, () -> sendMessage(
                    player,
                    messages.value().getGroupNotFound().replace("%group%", group)
                ));
            } else if (args[2].equalsIgnoreCase("maxServers")) {
                var state = Integer.parseInt(args[3]);
                NextCluster.instance().groupProvider().group(group).ifPresentOrElse(it -> {
                    it.asBuilder().withMaxOnline(state).publish();
                    sendMessage(
                        player,
                        messages.value().getGroupUpdated().replace("%group%", group)
                    );
                }, () -> sendMessage(
                    player,
                    messages.value().getGroupNotFound().replace("%group%", group)
                ));
            }
        }
        return true;
    }


    private void sendMessage(ProxiedPlayer player, String message) {
        if (!NextClusterPlugin.instance().messages().value().isMinimessage()) {
            player.sendMessage(message);
            return;
        }
        player.sendMessage(MiniMessage.miniMessage().deserialize(message).toString());
    }

    private void printServiceInformation(ProxiedPlayer player, ClusterService service, IngameMessages messages) {
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
                player, prefix + " <dark_gray>   » <gray>Platform: <white>" + information.getPlatform().id()
        );
        sendMessage(
                player, prefix + " <dark_gray>   » <gray>CPU: <white>" + information.getCpu() + "<dark_gray>%"
        );
        sendMessage(
                player, prefix + " <dark_gray>   » <gray>Memory: <white>" + information.getMemoryUsage() +
                        "<gray>MB"
        );
    }
}
