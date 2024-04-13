package net.nextcluster.driver.transmitter;

import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.codec.CodecBuffer;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Getter
@Accessors(fluent = true)
public class RedirectPacket extends Packet {
    private final String id;
    private final String className;
    private final String packetJson;

    public RedirectPacket(String id, Packet packet) {
        super();

        this.id = id;
        this.className = packet.getClass().getName();
        this.packetJson = JsonUtils.toJson(packet);

        this.getBuffer().writeString(this.id)
                .writeString(this.className)
                .writeString(this.packetJson);
    }

    public RedirectPacket(CodecBuffer buffer) {
        super(buffer);

        this.id = buffer.readString();
        this.className = buffer.readString();
        this.packetJson = buffer.readString();
    }
}