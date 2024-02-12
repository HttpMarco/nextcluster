package net.nextcluster.driver.resource.player.packets;

import net.nextcluster.driver.resource.player.ClusterPlayer;

public final class ClusterPlayerConnectPacket extends AbstractClusterPlayerPacket {

    public ClusterPlayerConnectPacket(ClusterPlayer clusterPlayer) {
        super(clusterPlayer);
    }
}
