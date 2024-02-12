package net.nextcluster.driver.event.players;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.event.ClusterEvent;
import net.nextcluster.driver.resource.player.ClusterPlayer;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class ClusterPlayerEvent implements ClusterEvent {

    private ClusterPlayer clusterPlayer;

}
