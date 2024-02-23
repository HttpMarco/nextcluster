package net.nextcluster.driver.event;

import dev.httpmarco.osgon.files.configuration.gson.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.networking.packets.ByteBuffer;
import net.nextcluster.driver.networking.packets.ClusterPacket;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ClusterEventCallPacket implements ClusterPacket {

    private ClusterEvent event;

    @Override
    public void write(ByteBuffer buffer) {
        buffer.writeString(buffer.getClass().getName());
        buffer.writeString(JsonUtils.toJson(event));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(ByteBuffer buffer) {
        try {
            Class<?> element = Class.forName(buffer.readString());
            this.event = JsonUtils.fromJson(buffer.readString(), (Class<ClusterEvent>) element);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
