package net.nextcluster.driver.event;

import dev.httpmarco.osgan.files.json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.nextcluster.driver.networking.packets.ByteBuffer;
import net.nextcluster.driver.networking.packets.ClusterPacket;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
public class ClusterEventCallPacket implements ClusterPacket {

    private String eventClass;
    private String json;

    public ClusterEventCallPacket(@NotNull ClusterEvent event) {
        this.eventClass = event.getClass().getName();
        this.json = JsonUtils.toJson(event);
    }

    public ClusterEventCallPacket(String eventClass, String json) {
        this.eventClass = eventClass;
        this.json = json;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.writeString(eventClass);
        buffer.writeString(json);
    }

    @Override
    public void read(ByteBuffer buffer) {
        this.eventClass = buffer.readString();
        this.json = buffer.readString();
    }
}
