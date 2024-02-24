package net.nextcluster.plugin.proxy.bungeecord;

import dev.httpmarco.osgan.utils.data.Pair;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.nextcluster.driver.resource.service.ServiceInformation;
import net.nextcluster.plugin.proxy.InternalClusterServer;
import net.nextcluster.plugin.proxy.NextClusterProxy;
import net.nextcluster.plugin.proxy.bungeecord.comand.BungeeCordClusterCommand;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class BungeeCordProxy extends NextClusterProxy {

    public BungeeCordProxy(Plugin plugin) {
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new BungeeCordClusterCommand(plugin, messages));
    }

    @Override
    public void registerServer(InternalClusterServer server) {
        final var info = ProxyServer.getInstance().constructServerInfo(
                server.name(),
                new InetSocketAddress(server.ip(), 25565),
                "NextCluster Service",
                false
        );
        ProxyServer.getInstance().getServers().put(server.name(), info);
    }

    @Override
    public void unregisterServer(InternalClusterServer server) {
        ProxyServer.getInstance().getServers().remove(server.name());
    }

    public Optional<ServerInfo> findFallback() {
        final var server = servers.values().stream().filter(InternalClusterServer::fallback).findFirst();
        return server.map(internalClusterServer -> ProxyServer.getInstance().getServerInfo(internalClusterServer.name()));
    }

    @Override
    public ServiceInformation currentInformation() {
        return new ServiceInformation(
                ProxyServer.getInstance().getOnlineCount(),
                ProxyServer.getInstance().getConfig().getPlayerLimit(),
                "BungeeCord",
                ProxyServer.getInstance().getPlayers()
                        .stream()
                        .map(player -> new Pair<>(player.getUniqueId(), player.getName()))
                        .toList()
        );
    }

    @Override
    public void dispatchCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }
}
