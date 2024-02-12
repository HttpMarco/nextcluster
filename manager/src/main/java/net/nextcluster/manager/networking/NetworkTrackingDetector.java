package net.nextcluster.manager.networking;

import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.event.ClusterEventCallPacket;

public class NetworkTrackingDetector {

    public static void detect() {
        NextCluster.instance().transmitter().registerListener(ClusterEventCallPacket.class, (channel, packet) -> {
            ((NettyServerTransmitter) NextCluster.instance().transmitter()).sendAllAndIgnoreSelf(channel, packet);
        });
    }
}
