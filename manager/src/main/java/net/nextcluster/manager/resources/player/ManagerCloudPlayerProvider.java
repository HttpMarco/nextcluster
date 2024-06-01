package net.nextcluster.manager.resources.player;

import dev.httpmarco.osgan.utils.executers.FutureResult;
import net.nextcluster.driver.transmitter.NetworkTransmitter;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.player.DefaultClusterPlayer;
import net.nextcluster.driver.resource.player.PlayerProvider;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerPacket;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerConnectPacket;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerDisconnectPacket;
import net.nextcluster.driver.resource.player.packets.ClusterPlayerResponsePacket;

import java.util.*;

public final class ManagerCloudPlayerProvider implements PlayerProvider {

    private final Map<UUID, ClusterPlayer> players = new HashMap<>();

    public ManagerCloudPlayerProvider(NetworkTransmitter transmitter) {
        transmitter.listen(ClusterPlayerConnectPacket.class, (channel, it) -> {
            var player = it.clusterPlayer();
            this.players.put(player.uniqueId(), player);
        });
        transmitter.listen(ClusterPlayerDisconnectPacket.class, (channel, it) -> this.players.remove(it.uniqueId()));

        transmitter.registerResponder("nextcluster_users_fetch", (channel, props) -> {
            if (props.has("uuid")) {
                return new ClusterPlayerPacket(this.player(props.readObject("uuid", UUID.class)).map(DefaultClusterPlayer::new).orElse(null));
            } else if (props.has("username")) {
                return new ClusterPlayerPacket(this.player(props.readString("username")).map(DefaultClusterPlayer::new).orElse(null));
            }

            return new ClusterPlayerPacket((DefaultClusterPlayer) null);
        });
        transmitter.registerResponder("nextcluster_users_online", (channel, props) -> {
            if (props.has("uuid")) {
                return new ClusterPlayerResponsePacket(this.players.containsKey(props.readObject("uuid", UUID.class)));
            } else if (props.has("username")) {
                return new ClusterPlayerResponsePacket(this.players.values().stream().anyMatch(clusterPlayer -> clusterPlayer.name().equals(props.readString("username"))));
            }

            return new ClusterPlayerResponsePacket(false);
        });
    }

    @Override
    public FutureResult<List<ClusterPlayer>> playersAsync() {
        var executor = new FutureResult<List<ClusterPlayer>>();
        executor.complete(players.values().stream().toList());
        return executor;
    }

    @Override
    public FutureResult<Optional<ClusterPlayer>> playerAsync(UUID uniqueId) {
        var executor = new FutureResult<Optional<ClusterPlayer>>();
        executor.complete(Optional.ofNullable(players.get(uniqueId)));
        return executor;
    }

    @Override
    public FutureResult<Optional<ClusterPlayer>> playerAsync(String name) {
        var executor = new FutureResult<Optional<ClusterPlayer>>();
        executor.complete(players.values().stream().filter(player -> player.name().equals(name)).findFirst());
        return executor;
    }

    @Override
    public FutureResult<Boolean> isOnlineAsync(String username) {
        var executor = new FutureResult<Boolean>();
        executor.complete(players.values().stream().anyMatch(player -> player.name().equals(username)));
        return executor;
    }

    @Override
    public FutureResult<Boolean> isOnlineAsync(UUID uniqueId) {
        var executor = new FutureResult<Boolean>();
        executor.complete(players.containsKey(uniqueId));
        return executor;
    }

    @Override
    public ClusterPlayer createPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        return new DefaultClusterPlayer(name, uniqueId, currentProxyName, currentServerName);
    }
}
