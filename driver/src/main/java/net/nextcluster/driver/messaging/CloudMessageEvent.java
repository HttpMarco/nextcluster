package net.nextcluster.driver.messaging;

import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.event.ClusterEvent;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class CloudMessageEvent implements ClusterEvent {

    private String channel;
    private String message;
    private JsonObjectSerializer data;

}
