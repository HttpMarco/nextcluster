package net.nextcluster.driver.resource.player.packets;

import dev.httpmarco.osgan.networking.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public final class ClusterPlayerResponsePacket implements Packet {
    private boolean online;
}
