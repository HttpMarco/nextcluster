package net.nextcluster.driver.messaging;

import dev.httpmarco.osgon.configuration.gson.JsonDocument;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.event.EventVisibility;

public class MessageService {

    public void sendGlobalMessage(String channelId, String message, JsonDocument data) {
        NextCluster.instance().eventRegistry().call(new CloudMessageEvent(channelId, message, data), EventVisibility.ONLY_OTHER);
    }

    public void sendGlobalMessage(String channelId, String message) {
        this.sendGlobalMessage(channelId, message, new JsonDocument());
    }
}
