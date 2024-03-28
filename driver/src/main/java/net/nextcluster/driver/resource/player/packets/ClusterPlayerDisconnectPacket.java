package net.nextcluster.driver.resource.player.packets;

import dev.httpmarco.osgan.networking.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public final class ClusterPlayerDisconnectPacket implements Packet {
    private UUID uniqueId;
}
