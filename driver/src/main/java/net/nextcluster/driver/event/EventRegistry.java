package net.nextcluster.driver.event;

import dev.httpmarco.osgan.reflections.Reflections;
import dev.httpmarco.osgan.utils.data.Pair;
import dev.httpmarco.osgan.utils.types.ListUtils;
import lombok.SneakyThrows;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.messaging.CloudMessageEvent;

import java.lang.reflect.Method;
import java.util.*;

public class EventRegistry {

    private final Map<Class<? extends ClusterEvent>, List<Pair<Object, Method>>> eventRegistry = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void registerListener(Object listener) {
        for (var method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ClusterListener.class)) {
                var types = method.getParameterTypes();
                if (types.length == 0 || !ClusterEvent.class.isAssignableFrom(types[0])) {
                    continue;
                }
                eventRegistry.put((Class<? extends ClusterEvent>) types[0],
                        ListUtils.append(eventRegistry.getOrDefault(types[0],
                                new ArrayList<>()), new Pair<>(listener, method)));
            }
        }
    }

    @SneakyThrows
    public void callLocal(ClusterEvent event) {
        if (eventRegistry.containsKey(event.getClass())) {
            for (var listener : eventRegistry.get(event.getClass()).stream().sorted(Comparator.comparing(it -> it.getValue().getAnnotation(ClusterListener.class).priority())).toList()) {
                listener.getKey().getClass().getDeclaredMethod(listener.getValue().getName(), event.getClass()).invoke(listener.getKey(), event);
            }
        }
    }

    public void call(ClusterEvent event, EventVisibility visibility) {
        if (visibility == EventVisibility.ALL || visibility == EventVisibility.ONLY_SELF || visibility == EventVisibility.SAME_CHANNEL_ID) {
            this.callLocal(event);
        }
        if (visibility == EventVisibility.ALL || visibility == EventVisibility.ONLY_OTHER) {
            NextCluster.instance().transmitter().send(new ClusterEventCallPacket(event));
        } else if (visibility == EventVisibility.SAME_CHANNEL_ID) {
            var channelId = System.getenv("NETTY_CLIENT_ID");

            if (channelId != null) {
                NextCluster.instance().transmitter().redirect(channelId, new ClusterEventCallPacket(event));
            } else {
                NextCluster.LOGGER.warn("Called event with visibility 'SAME_CHANNEL_ID' but no channel environment variable is set ('NETTY_CLIENT_ID')");
            }
        }
    }

    public void call(ClusterEvent event) {
        this.call(event, EventVisibility.ALL);
    }
}
