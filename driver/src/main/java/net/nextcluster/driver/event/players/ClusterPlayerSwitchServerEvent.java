package net.nextcluster.driver.event.players;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.service.ClusterService;

@Getter
@Accessors(fluent = true)
public final class ClusterPlayerSwitchServerEvent extends ClusterPlayerEvent {

    private final ClusterService currentService;
    private final ClusterService previousService;

    public ClusterPlayerSwitchServerEvent(ClusterPlayer clusterPlayer, ClusterService currentService, ClusterService previousService) {
        super(clusterPlayer);
        this.currentService = currentService;
        this.previousService = previousService;
    }
}
