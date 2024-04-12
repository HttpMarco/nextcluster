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

package net.nextcluster.driver.transmitter;

import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.listening.ChannelPacketListener;
import dev.httpmarco.osgan.networking.request.PacketResponder;
import io.netty5.channel.Channel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class NetworkTransmitter {

    public static final Integer NETTY_PORT = Integer.parseInt(System.getProperty("netty.port", "9090"));

    private final AtomicBoolean isServer = new AtomicBoolean();

    public NetworkTransmitter() {
        try {
            Class.forName("net.nextcluster.manager.NextClusterManager");
            this.isServer.set(true);
        } catch (ClassNotFoundException ignored) {
        }
    }

    public abstract void send(Packet packet);

    public abstract void send(Channel channel, Packet packet);

    public abstract void redirect(String id, Packet packet);

    public abstract <P extends Packet> void listen(Class<P> packetClass, ChannelPacketListener<P> listener);

    public abstract <T extends Packet> void request(String id, Class<T> responsePacket, Consumer<T> consumer);

    public abstract <T extends Packet> void request(String id, JsonObjectSerializer properties, Class<T> responsePacket, Consumer<T> consumer);

    public abstract <T extends Packet> void registerResponder(String id, PacketResponder<T> responder);

    public boolean isServer() {
        return this.isServer.get();
    }

}