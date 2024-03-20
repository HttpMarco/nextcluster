package net.nextcluster.manager.networking;

import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.event.ClusterEventCallPacket;
import net.nextcluster.driver.networking.packets.ForwardPacket;

public class NetworkTrackingDetector {

    public static void detect() {
        NextCluster.instance().transmitter().registerListener(ClusterEventCallPacket.class, (channel, packet) ->
                ((NettyServerTransmitter) NextCluster.instance().transmitter()).sendAllAndIgnoreSelf(channel, packet));

        NextCluster.instance().transmitter().registerListener(ForwardPacket.class, (channel, packet) ->
                ((NettyServerTransmitter) NextCluster.instance().transmitter()).sendAllAndIgnoreSelf(channel, packet));
    }
}
