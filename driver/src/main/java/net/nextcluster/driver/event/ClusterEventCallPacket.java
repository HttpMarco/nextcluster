package net.nextcluster.driver.event;

import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.osgan.networking.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ClusterEventCallPacket implements Packet {
    private String eventClass;
    private String json;

    public ClusterEventCallPacket(@NotNull ClusterEvent event) {
        this.eventClass = event.getClass().getName();
        this.json = JsonUtils.toJson(event);
    }
}
