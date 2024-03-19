package net.nextcluster.driver.messaging;

import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.event.EventVisibility;

public class MessageService {

    public void sendGlobalMessage(String channelId, String message, JsonObjectSerializer data) {
        NextCluster.instance().eventRegistry().call(new CloudMessageEvent(channelId, message, data), EventVisibility.ONLY_OTHER);
    }

    public void sendGlobalMessage(String channelId, String message) {
        this.sendGlobalMessage(channelId, message, new JsonObjectSerializer());
    }
}
