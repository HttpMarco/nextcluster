package net.nextcluster.driver.event;

import dev.httpmarco.osgan.files.json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.networking.packets.ByteBuffer;
import net.nextcluster.driver.networking.packets.ClusterPacket;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ClusterEventCallPacket implements ClusterPacket {

    private String eventClass;
    private String event;

    public ClusterEventCallPacket(ClusterEvent event) {
        this.eventClass = event.getClass().getName();
        this.event = JsonUtils.toJson(event);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.writeString(eventClass);
        buffer.writeString(event);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(ByteBuffer buffer) {
        this.eventClass = buffer.readString();
        this.event = buffer.readString();
    }
}
