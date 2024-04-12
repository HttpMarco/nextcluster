package net.nextcluster.driver.transmitter;

import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.codec.CodecBuffer;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ChannelIdPacket extends Packet {
    private final String id;

    public ChannelIdPacket(String id) {
        super();

        this.id = id;

        this.getBuffer().writeString(this.id);
    }

    public ChannelIdPacket(CodecBuffer buffer) {
        super(buffer);

        this.id = buffer.readString();
    }
}
