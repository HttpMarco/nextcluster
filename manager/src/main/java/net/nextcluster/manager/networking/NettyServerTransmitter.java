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

package net.nextcluster.manager.networking;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import dev.httpmarco.osgan.networking.ChannelTransmit;
import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.listening.ChannelPacketListener;
import dev.httpmarco.osgan.networking.request.PacketResponder;
import dev.httpmarco.osgan.networking.server.NettyServer;
import io.netty5.channel.Channel;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.transmitter.NetworkTransmitter;
import net.nextcluster.driver.transmitter.RedirectPacket;
import net.nextcluster.manager.NextClusterManager;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@Accessors(fluent = true)
public class NettyServerTransmitter extends NetworkTransmitter {
    private final Map<String, List<ChannelTransmit>> transmitsById = Maps.newHashMap();

    public void registerTransmitter(String id, ChannelTransmit transmit) {
        if (!this.transmitsById.containsKey(id)) {
            this.transmitsById.put(id, Lists.newArrayList());
        }

        this.transmitsById.get(id).add(transmit);

        NextCluster.LOGGER.info("Channel {} registered with id '{}'", transmit.channel().remoteAddress(), id);
    }

    public void unregisterTransmitter(ChannelTransmit transmit) {
        this.transmitsById.forEach((s, channelTransmits) -> channelTransmits.remove(transmit));
    }

    @Override
    public void send(Packet packet) {
        this.nettyServer().sendPacket(packet);
    }

    @Override
    public void send(Channel channel, Packet packet) {
        this.nettyServer().sendPacket(channel, packet);
    }

    @Override
    public void redirect(String id, Packet packet) {
        this.doRedirect(id, new RedirectPacket(id, packet));
    }

    public void sendAllAndIgnoreSelf(Channel incomingChannel, Packet packet) {
        this.nettyServer().sendPacketAndIgnoreSelf(incomingChannel, packet);
    }

    @Override
    public <P extends Packet> void listen(Class<P> packetClass, ChannelPacketListener<P> listener) {
        this.nettyServer().listen(packetClass, listener);
    }

    @Override
    public <T extends Packet> void request(String id, Class<T> responsePacket, Consumer<T> consumer) {
        this.nettyServer().request(id, new JsonObjectSerializer(), responsePacket, consumer);
    }

    @Override
    public <T extends Packet> void request(String id, JsonObjectSerializer properties, Class<T> responsePacket, Consumer<T> consumer) {
        this.nettyServer().request(id, properties, responsePacket, consumer);
    }

    @Override
    public <T extends Packet> void registerResponder(String id, PacketResponder<T> responder) {
        this.nettyServer().registerResponder(id, responder);
    }

    public void doRedirect(String id, Packet packet) {
        var matchingTransmits = this.transmitsById.getOrDefault(id, Lists.newArrayList());

        if (!matchingTransmits.isEmpty()) {
            matchingTransmits.forEach(matching -> matching.sendPacket(packet));
        }
    }

    private NettyServer nettyServer() {
        return ((NextClusterManager) NextCluster.instance()).nettyServer();
    }
}
