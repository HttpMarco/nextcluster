/*
 * MIT License
 *
 * Copyright (c) 2024 nextCluster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.nextcluster.driver.networking.transmitter;

import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.osgan.utils.types.ListUtils;
import io.netty5.channel.Channel;
import net.nextcluster.driver.networking.packets.ClusterPacket;
import net.nextcluster.driver.networking.packets.PacketListener;
import net.nextcluster.driver.networking.packets.PacketResponder;
import net.nextcluster.driver.networking.request.RequestPacket;
import net.nextcluster.driver.networking.request.ResponderRegistrationPacket;
import net.nextcluster.driver.resource.player.ClusterPlayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class NetworkTransmitter {

    private final Map<String, PacketResponder<?>> responders = new HashMap<>();
    private final Map<UUID, Consumer<ClusterPacket>> requests = new HashMap<>();
    private final Map<UUID, Class<? extends ClusterPacket>> requestClass = new HashMap<>();
    private final Map<Class<? extends ClusterPacket>, List<PacketListener<ClusterPacket>>> listeners = new HashMap<>();

    private final NetworkTransmitterDetector detector;
    private final AtomicBoolean isServer = new AtomicBoolean();

    public NetworkTransmitter() {
        this.detector = new NetworkTransmitterDetector(this);

        try {
            Class.forName("net.nextcluster.manager.NextClusterManager");
            this.isServer.set(true);
        } catch (ClassNotFoundException ignored) {
        }
    }

    public void unregisterChannel(Channel channel) {
        this.detector.unregisterChannel(channel);
    }

    public void call(Channel channel, ClusterPacket clusterPacket) {
        if (this.listeners.containsKey(clusterPacket.getClass())) {
            this.listeners.get(clusterPacket.getClass()).forEach(listener -> listener.onReceive(channel, clusterPacket));
        }
    }

    public abstract void send(ClusterPacket packet);

    public abstract void send(Channel channel, ClusterPacket packet);

    public abstract <T extends ClusterPacket> void forward(T t);

    @SuppressWarnings("unchecked")
    public <T extends ClusterPacket> void registerListener(Class<T> searchedClassed, PacketListener<T> listener) {
        listeners.put(searchedClassed, ListUtils.append(listeners.getOrDefault(searchedClassed, new ArrayList<>()), (PacketListener<ClusterPacket>) listener));
    }

    @SuppressWarnings("unchecked")
    public <T extends ClusterPacket> void request(String id, JsonObjectSerializer requestDocument, Class<T> responsePacket, Consumer<T> consumer) {
        var uniqueId = UUID.randomUUID();
        this.send(new RequestPacket(id, uniqueId, requestDocument));
        this.requests.put(uniqueId, (Consumer<ClusterPacket>) consumer);
        this.requestClass.put(uniqueId, responsePacket);
    }

    public <T extends ClusterPacket> void request(String id, Class<T> responsePacket, Consumer<T> consumer) {
        this.request(id, new JsonObjectSerializer(), responsePacket, consumer);
    }

    public <T extends ClusterPacket> void setResponder(String id, PacketResponder<T> responder) {
        this.responders.put(id, responder);

        if (!this.isServer()) {
            this.send(new ResponderRegistrationPacket(id));
        }
    }

    public boolean isRequestPresent(UUID id) {
        return this.requests.containsKey(id);
    }

    public void acceptRequests(UUID id, String response) {
        this.requests.get(id).accept(JsonUtils.fromJson(response, this.requestClass.get(id)));
        this.removeRequest(id);
    }

    public void removeRequest(UUID id) {
        this.requests.remove(id);
        this.requestClass.remove(id);
    }

    public boolean isResponderPresent(String id) {
        return this.responders.containsKey(id);
    }

    public PacketResponder<?> getResponder(String id) {
        return this.responders.get(id);
    }

    public boolean isServer() {
        return this.isServer.get();
    }

}