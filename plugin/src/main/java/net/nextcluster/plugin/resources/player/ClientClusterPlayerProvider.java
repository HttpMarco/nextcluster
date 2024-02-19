package net.nextcluster.plugin.resources.player;

import dev.httpmarco.osgan.utils.executers.ThreadAsyncExecutor;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.player.PlayerProvider;

import java.util.Optional;
import java.util.UUID;

// TODO: register on nextCluster
public final class ClientClusterPlayerProvider implements PlayerProvider {


    @Override
    public ThreadAsyncExecutor<Optional<ClusterPlayer>> getPlayerAsync(UUID uniqueId) {
        return null;
    }

    @Override
    public ThreadAsyncExecutor<Optional<ClusterPlayer>> getPlayerAsync(String name) {
        return null;
    }

    @Override
    public ThreadAsyncExecutor<Boolean> isOnlineAsync(String username) {
        return null;
    }

    @Override
    public ThreadAsyncExecutor<Boolean> isOnlineAsync(UUID uniqueId) {
        return null;
    }

    @Override
    public ClusterPlayer createPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        return null;
    }
}
