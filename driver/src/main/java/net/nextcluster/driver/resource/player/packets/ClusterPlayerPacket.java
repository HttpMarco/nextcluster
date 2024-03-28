package net.nextcluster.driver.resource.player.packets;

import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.annotation.PacketIncludeObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.resource.player.ClusterPlayer;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ClusterPlayerPacket implements Packet {
    @PacketIncludeObject
    private ClusterPlayer clusterPlayer;
}
