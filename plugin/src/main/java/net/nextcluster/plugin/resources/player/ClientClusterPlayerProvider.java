package net.nextcluster.plugin.resources.player;

import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.player.PlayerProvider;

import java.util.Optional;
import java.util.UUID;

// TODO: register on nextCluster
public final class ClientClusterPlayerProvider implements PlayerProvider {

    @Override
    public Optional<ClusterPlayer> getPlayer(UUID uniqueId) {
        // TODO Performance: First check local cache -> Than request from server
        return Optional.empty();
    }

    @Override
    public Optional<ClusterPlayer> getPlayer(String name) {
        // TODO Performance: First check local cache -> Than request from server
        return Optional.empty();
    }

    @Override
    public boolean isOnline(String username) {
        // TODO Performance: First check local cache -> Than request from server
        return false;
    }

    @Override
    public boolean isOnline(UUID uniqueId) {
        // TODO Performance: First check local cache -> Than request from server
        return false;
    }

    @Override
    public ClusterPlayer createPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        return null;
    }

}
