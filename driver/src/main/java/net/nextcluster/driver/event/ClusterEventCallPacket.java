package net.nextcluster.driver.event;

import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.codec.CodecBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
public class ClusterEventCallPacket extends Packet {
    private final String eventClass;
    private final String json;

    public ClusterEventCallPacket(@NotNull ClusterEvent event) {
        super();

        this.eventClass = event.getClass().getName();
        this.json = JsonUtils.toJson(event);

        this.getBuffer().writeString(this.eventClass)
                .writeString(this.json);
    }

    public ClusterEventCallPacket(CodecBuffer buffer) {
        super(buffer);

        this.eventClass = buffer.readString();
        this.json = buffer.readString();
    }
}
