package net.nextcluster.driver.resource.player.packets;

import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.codec.CodecBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class ClusterPlayerResponsePacket extends Packet {
    private final boolean online;

    public ClusterPlayerResponsePacket(boolean online) {
        this.online = online;

        this.getBuffer().writeBoolean(online);
    }

    public ClusterPlayerResponsePacket(CodecBuffer buffer) {
        super(buffer);

        this.online = buffer.readBoolean();
    }
}
