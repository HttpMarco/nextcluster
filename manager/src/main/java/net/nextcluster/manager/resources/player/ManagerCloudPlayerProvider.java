package net.nextcluster.manager.resources.player;

import net.nextcluster.driver.NextCluster;
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

    public ManagerCloudPlayerProvider() {
        // TODO: Fix @Mirco
        /*NextCluster.instance().transmitter().registerListener(ClusterPlayerConnectPacket.class, (channel, it) -> {
            this.players.put(it.clusterPlayer().uniqueId(), it.clusterPlayer());
        });
        NextCluster.instance().transmitter().registerListener(ClusterPlayerDisconnectPacket.class, (channel, it) -> {
            this.players.remove(it.uniqueId());
        });*/
    }

    @Override
    public Optional<ClusterPlayer> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(players.get(uniqueId));
    }

    @Override
    public Optional<ClusterPlayer> getPlayer(String name) {
        return players.values().stream().filter(player -> player.name().equals(name)).findFirst();
    }

    @Override
    public boolean isOnline(String username) {
        return players.values().stream().anyMatch(player -> player.name().equals(username));
    }

    @Override
    public boolean isOnline(UUID uniqueId) {
        return players.containsKey(uniqueId);
    }

    @Override
    public ClusterPlayer createPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        return null;
    }
}
