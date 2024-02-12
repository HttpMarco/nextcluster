package net.nextcluster.driver.resource.player.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.networking.packets.ByteBuffer;
import net.nextcluster.driver.networking.packets.ClusterPacket;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public final class ClusterPlayerDisconnectPacket implements ClusterPacket {

    private UUID uniqueId;

    @Override
    public void write(ByteBuffer buffer) {
        buffer.writeUUID(this.uniqueId);
    }

    @Override
    public void read(ByteBuffer buffer) {
        this.uniqueId = buffer.readUUID();
    }
}
