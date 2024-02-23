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

package net.nextcluster.plugin.proxy.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.httpmarco.osgan.utils.data.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerConnectPacket;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerDisconnectPacket;
import net.nextcluster.driver.resource.service.ServiceInformation;
import net.nextcluster.plugin.proxy.InternalClusterServer;
import net.nextcluster.plugin.proxy.NextClusterProxy;

import java.net.InetSocketAddress;
import java.util.Optional;


@Plugin(
    id = "nextcluster",
    name = "NextCluster",
    version = "1.0.0",
    url = "https://nextcluster.net",
    authors = {"NextCluster"}
)
public class NextClusterVelocity extends NextClusterProxy {

    private final ProxyServer server;

    @Inject
    public NextClusterVelocity(ProxyServer server) {
        this.server = server;
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        for (RegisteredServer registered : this.server.getAllServers()) {
            this.server.unregisterServer(registered.getServerInfo());
        }

        watch();
    }

    @Override
    public void registerServer(InternalClusterServer server) {
        this.server.registerServer(new ServerInfo(
            server.name(), new InetSocketAddress(server.ip(), 25565)
        ));
    }

    @Override
    public void unregisterServer(InternalClusterServer server) {
        this.server.unregisterServer(this.server.getServer(server.name()).orElseThrow().getServerInfo());
    }

    @Subscribe
    public void onPlayerConnect(ServerConnectedEvent event) {
        if (event.getPreviousServer().isPresent()) {

        } else {
            NextCluster.instance().transmitter().send(new ClusterPlayerConnectPacket(new VelocityClusterPlayer(this.server, event.getPlayer())));
        }
    }

    @Subscribe
    public void onPlayerConnect(DisconnectEvent event) {
        NextCluster.instance().transmitter().send(new ClusterPlayerDisconnectPacket(event.getPlayer().getUniqueId()));
    }

    @Subscribe
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        findFallback().ifPresentOrElse(
            event::setInitialServer,
            () -> event.getPlayer().disconnect(Component.text("No server available!"))
        );
    }

    @Subscribe
    public void onServerKick(KickedFromServerEvent event) {
        final var fallback = findFallback();
        if (fallback.isEmpty()) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("No server available!")));
            return;
        }
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback.get()));
    }

    private Optional<RegisteredServer> findFallback() {
        final var server = servers.values().stream().filter(InternalClusterServer::fallback).findFirst();
        if (server.isEmpty()) {
            return Optional.empty();
        }
        return this.server.getServer(server.get().name());
    }

    @Override
    public ServiceInformation currentInformation() {
        return new ServiceInformation(
            this.server.getPlayerCount(),
            this.server.getConfiguration().getShowMaxPlayers(),
            ((TextComponent) this.server.getConfiguration().getMotd()).content(),
            this.server.getAllPlayers()
                .stream()
                .map(player -> new Pair<>(player.getUniqueId(), player.getUsername()))
                .toList()
        );
    }
}
