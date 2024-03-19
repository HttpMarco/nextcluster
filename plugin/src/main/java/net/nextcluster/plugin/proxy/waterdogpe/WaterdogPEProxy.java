package net.nextcluster.plugin.proxy.waterdogpe;

import dev.httpmarco.osgan.utils.data.Pair;

import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.BedrockServerInfo;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import net.nextcluster.driver.resource.service.ServiceInformation;
import net.nextcluster.plugin.proxy.InternalClusterServer;
import net.nextcluster.plugin.proxy.NextClusterProxy;
import net.nextcluster.plugin.proxy.waterdogpe.command.WaterdogPEClusterCommand;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class WaterdogPEProxy extends NextClusterProxy {

    public WaterdogPEProxy() {
        ProxyServer.getInstance().getCommandMap().registerCommand(new WaterdogPEClusterCommand(messages));
    }

    @Override
    public void registerServer(InternalClusterServer server) {
        final var info = new BedrockServerInfo(
                server.name(),
                new InetSocketAddress(server.ip(), 19132),
                new InetSocketAddress(server.ip(), 19132)
        );
        ProxyServer.getInstance().registerServerInfo(info);
    }

    @Override
    public void unregisterServer(InternalClusterServer server) {
        ProxyServer.getInstance().removeServerInfo(server.name());
    }

    public Optional<ServerInfo> findFallback() {
        final var server = servers.values().stream().filter(InternalClusterServer::fallback).findFirst();
        return server.map(internalClusterServer -> ProxyServer.getInstance().getServerInfo(internalClusterServer.name()));
    }

    @Override
    public ServiceInformation currentInformation() {
        return new ServiceInformation(
                ProxyServer.getInstance().getPlayers().size(),
                ProxyServer.getInstance().getConfiguration().getMaxPlayerCount(),
                "WaterdogPE",
                ProxyServer.getInstance().getPlayers()
                        .values()
                        .stream()
                        .map(player -> new Pair<>(player.getUniqueId(), player.getName()))
                        .toList()
        );
    }

    @Override
    public void dispatchCommand(String command) {
        ProxyServer.getInstance().dispatchCommand(ProxyServer.getInstance().getConsoleSender(), command);
    }
}
