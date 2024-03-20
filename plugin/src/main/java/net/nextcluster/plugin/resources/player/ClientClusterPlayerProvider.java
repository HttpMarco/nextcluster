package net.nextcluster.plugin.resources.player;

import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import dev.httpmarco.osgan.utils.executers.ThreadAsyncExecutor;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.player.PlayerProvider;
import net.nextcluster.driver.resource.player.packets.AbstractClusterPlayerPacket;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerResponsePacket;
import net.nextcluster.plugin.NextClusterPlugin;
import net.nextcluster.plugin.proxy.NextClusterProxy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ClientClusterPlayerProvider implements PlayerProvider {

    @Override
    public ThreadAsyncExecutor<List<ClusterPlayer>> playersAsync() {
        // todo: implement
        return null;
    }

    @Override
    public ThreadAsyncExecutor<Optional<ClusterPlayer>> playerAsync(UUID uniqueId) {
        var executor = new ThreadAsyncExecutor<Optional<ClusterPlayer>>();
        NextCluster.instance().transmitter().request("nextcluster_users_fetch", new JsonObjectSerializer().append("uuid", uniqueId),
                AbstractClusterPlayerPacket.class, it -> executor.complete(Optional.of(it.clusterPlayer())));
        return executor;
    }

    @Override
    public ThreadAsyncExecutor<Optional<ClusterPlayer>> playerAsync(String username) {
        var executor = new ThreadAsyncExecutor<Optional<ClusterPlayer>>();
        NextCluster.instance().transmitter().request("nextcluster_users_fetch", new JsonObjectSerializer().append("username", username),
                AbstractClusterPlayerPacket.class, it -> executor.complete(Optional.of(it.clusterPlayer())));
        return executor;
    }

    @Override
    public ThreadAsyncExecutor<Boolean> isOnlineAsync(UUID uniqueId) {
        var executor = new ThreadAsyncExecutor<Boolean>();
        NextCluster.instance().transmitter().request("nextcluster_users_online", new JsonObjectSerializer().append("uuid", uniqueId.toString()),
                ClusterPlayerResponsePacket.class, it -> executor.complete(it.online()));
        return executor;
    }

    @Override
    public ThreadAsyncExecutor<Boolean> isOnlineAsync(String username) {
        var executor = new ThreadAsyncExecutor<Boolean>();
        NextCluster.instance().transmitter().request("nextcluster_users_online", new JsonObjectSerializer().append("username", username),
                ClusterPlayerResponsePacket.class, it -> executor.complete(it.online()));
        return executor;
    }

    @Override
    public ClusterPlayer createPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        return ((NextClusterProxy) NextClusterPlugin.instance()).buildPlayer(uniqueId, currentProxyName);
    }
}
