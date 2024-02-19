package net.nextcluster.manager.resources.player;

import dev.httpmarco.osgan.utils.executers.ThreadAsyncExecutor;
import net.nextcluster.driver.networking.transmitter.NetworkTransmitter;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.player.PlayerProvider;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerConnectPacket;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerDisconnectPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ManagerCloudPlayerProvider implements PlayerProvider {

    private final Map<UUID, ClusterPlayer> players = new HashMap<>();

    public ManagerCloudPlayerProvider(NetworkTransmitter transmitter) {
        transmitter.registerListener(ClusterPlayerConnectPacket.class, (channel, it) -> this.players.put(it.clusterPlayer().uniqueId(), it.clusterPlayer()));
        transmitter.registerListener(ClusterPlayerDisconnectPacket.class, (channel, it) -> this.players.remove(it.uniqueId()));
    }

    @Override
    public ThreadAsyncExecutor<Optional<ClusterPlayer>> getPlayerAsync(UUID uniqueId) {
        var executor = new ThreadAsyncExecutor<Optional<ClusterPlayer>>();
        executor.complete(Optional.ofNullable(players.get(uniqueId)));
        return executor;
    }

    @Override
    public ThreadAsyncExecutor<Optional<ClusterPlayer>> getPlayerAsync(String name) {
        var executor = new ThreadAsyncExecutor<Optional<ClusterPlayer>>();
        executor.complete(players.values().stream().filter(player -> player.name().equals(name)).findFirst());
        return executor;
    }

    @Override
    public ThreadAsyncExecutor<Boolean> isOnlineAsync(String username) {
        var executor = new ThreadAsyncExecutor<Boolean>();
        executor.complete(players.values().stream().anyMatch(player -> player.name().equals(username)));
        return executor;
    }

    @Override
    public ThreadAsyncExecutor<Boolean> isOnlineAsync(UUID uniqueId) {
        var executor = new ThreadAsyncExecutor<Boolean>();
        executor.complete(players.containsKey(uniqueId));
        return executor;
    }

    @Override
    public ClusterPlayer createPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        return null;
    }
}
