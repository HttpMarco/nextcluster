package net.nextcluster.driver.networking.packets;

import dev.httpmarco.osgan.files.json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ForwardPacket implements ClusterPacket {
    private String packetClass;
    private String packetJson;

    public ForwardPacket(ClusterPacket packet) {
        this.packetClass = packet.getClass().getName();
        this.packetJson = JsonUtils.toJson(packet);
    }

    @SneakyThrows
    public ClusterPacket packet() {
        return JsonUtils.fromJson(packetJson, (Class<? extends ClusterPacket>) Class.forName(this.packetClass));
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.writeString(packetClass);
        buffer.writeString(packetJson);
    }

    @Override
    public void read(ByteBuffer buffer) {
        this.packetClass = buffer.readString();
        this.packetJson = buffer.readString();
    }
}
