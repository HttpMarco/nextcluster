package net.nextcluster.driver.event;

import dev.httpmarco.osgan.reflections.Reflections;
import dev.httpmarco.osgan.utils.data.Pair;
import dev.httpmarco.osgan.utils.types.ListUtils;
import net.nextcluster.driver.NextCluster;

import java.lang.reflect.Method;
import java.util.*;

public class EventRegistry {

    private final Map<Class<? extends ClusterEvent>, List<Pair<Object, Method>>> eventRegistry = new HashMap<>();

    public EventRegistry() {
        NextCluster.instance().transmitter().registerListener(ClusterEventCallPacket.class, (channel, packet) -> callLocal(packet.event()));
    }

    @SuppressWarnings("unchecked")
    public void registerListener(Object listener) {
        for (var method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ClusterListener.class)) {
                var types = method.getParameterTypes();
                if (types.length == 0 || !types[0].isAssignableFrom(ClusterEvent.class)) {
                    continue;
                }
                eventRegistry.put((Class<? extends ClusterEvent>) types[0],
                        ListUtils.append(eventRegistry.getOrDefault(types[0],
                                new ArrayList<>()), new Pair<>(listener, method)));
            }
        }
    }

    private void callLocal(ClusterEvent event) {
        if (eventRegistry.containsKey(event.getClass())) {
            for (var listener : eventRegistry.get(event.getClass()).stream().sorted(Comparator.comparing(it -> it.getValue().getAnnotation(ClusterListener.class).priority())).toList()) {
                Reflections.callMethod(listener.getValue(), listener.getKey(), event);
            }
        }
    }

    public void call(ClusterEvent event, EventVisibility visibility) {
        if (visibility == EventVisibility.ALL || visibility == EventVisibility.ONLY_SELF) {
            this.callLocal(event);
        }
        if (visibility == EventVisibility.ALL || visibility == EventVisibility.ONLY_OTHER) {
            NextCluster.instance().transmitter().send(new ClusterEventCallPacket(event));
        }
    }

    public void call(ClusterEvent event) {
        this.call(event, EventVisibility.ALL);
    }
}
