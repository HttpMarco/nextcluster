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

package net.nextcluster.prevm.networking;

import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.client.NettyClient;
import dev.httpmarco.osgan.networking.listening.ChannelPacketListener;
import dev.httpmarco.osgan.networking.request.PacketResponder;
import io.netty5.channel.Channel;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.transmitter.NetworkTransmitter;
import net.nextcluster.driver.transmitter.RedirectPacket;
import net.nextcluster.prevm.PreVM;

import java.util.function.Consumer;

@Setter
@Accessors(fluent = true)
public class NettyClientTransmitter extends NetworkTransmitter {
    @Override
    public void send(Packet packet) {
        this.nettyClient().sendPacket(packet);
    }

    @Override
    public void send(Channel channel, Packet packet) {
        this.nettyClient().sendPacket(channel, packet);
    }

    @Override
    public void redirect(String id, Packet packet) {
        this.send(new RedirectPacket(id, packet));
    }

    @Override
    public <P extends Packet> void listen(Class<P> packetClass, ChannelPacketListener<P> listener) {
        this.nettyClient().listen(packetClass, listener);
    }

    @Override
    public <T extends Packet> void request(String id, Class<T> responsePacket, Consumer<T> consumer) {
        this.nettyClient().request(id, new JsonObjectSerializer(), responsePacket, consumer);
    }

    @Override
    public <T extends Packet> void request(String id, JsonObjectSerializer properties, Class<T> responsePacket, Consumer<T> consumer) {
        this.nettyClient().request(id, properties, responsePacket, consumer);
    }

    @Override
    public <T extends Packet> void registerResponder(String id, PacketResponder<T> responder) {
        this.nettyClient().registerResponder(id, responder);
    }

    private NettyClient nettyClient() {
        return ((PreVM) NextCluster.instance()).nettyClient();
    }
}
