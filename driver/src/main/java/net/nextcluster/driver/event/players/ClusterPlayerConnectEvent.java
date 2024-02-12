package net.nextcluster.driver.event.players;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.resource.player.ClusterPlayer;

@Getter
@Accessors(fluent = true)
public final class ClusterPlayerConnectEvent extends ClusterPlayerEvent {

    public ClusterPlayerConnectEvent(ClusterPlayer clusterPlayer) {
        super(clusterPlayer);
    }
}
