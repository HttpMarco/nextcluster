package net.nextcluster.driver.resource.player.packets;

import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.codec.CodecBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class ClusterPlayerDisconnectPacket extends Packet {
    private UUID uniqueId;

    public ClusterPlayerDisconnectPacket(UUID uniqueId) {
        this.uniqueId = uniqueId;

        this.getBuffer().writeUniqueId(uniqueId);
    }

    public ClusterPlayerDisconnectPacket(CodecBuffer buffer) {
        super(buffer);

        this.uniqueId = buffer.readUniqueId();
    }
}
