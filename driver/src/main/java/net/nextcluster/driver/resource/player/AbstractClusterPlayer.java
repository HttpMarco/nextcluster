package net.nextcluster.driver.resource.player;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.service.ClusterService;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public abstract class AbstractClusterPlayer implements ClusterPlayer {

    private final String name;
    private final UUID uniqueId;

    private final String connectedProxyName;
    private final String connectedServerName;

    public AbstractClusterPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        this.name = name;
        this.uniqueId = uniqueId;
        this.connectedProxyName = currentProxyName;
        this.connectedServerName = currentServerName;
    }

    @Override
    public ClusterService connectedClusterServer() {
        return NextCluster.instance().serviceProvider().getService(connectedServerName).orElse(null);
    }

    @Override
    public ClusterService connectedClusterProxy() {
        return NextCluster.instance().serviceProvider().getService(connectedProxyName).orElse(null);
    }
}
