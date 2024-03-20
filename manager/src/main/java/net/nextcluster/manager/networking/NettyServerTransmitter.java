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

import io.netty5.channel.Channel;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.networking.packets.ClusterPacket;
import net.nextcluster.driver.networking.transmitter.NetworkTransmitter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class NettyServerTransmitter extends NetworkTransmitter {

    private final List<Channel> channels = new ArrayList<>();

    @Override
    public void send(ClusterPacket packet) {
        for (Channel channel : channels) {
            this.send(channel, packet);
        }
    }

    @Override
    public void send(Channel channel, ClusterPacket packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public <T extends ClusterPacket> void forward(T t) {
        send(t);
    }

    public void sendAllAndIgnoreSelf(Channel incomingChannel, ClusterPacket packet) {
        for (var channel : channels()) {
            if (!incomingChannel.equals(channel)) {
                channel.writeAndFlush(packet);
            }
        }
    }
}
