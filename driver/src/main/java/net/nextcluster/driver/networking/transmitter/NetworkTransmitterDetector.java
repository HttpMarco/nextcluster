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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.httpmarco.osgan.utils.RandomUtils;
import io.netty5.channel.Channel;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.networking.request.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public final class NetworkTransmitterDetector {

    private final Map<UUID, PendingRequest> pending = Maps.newHashMap();
    private final Map<String, ArrayList<Channel>> responders = Maps.newHashMap();
    private final Map<Channel, String> respondersByChannel = Maps.newHashMap();

    public NetworkTransmitterDetector(NetworkTransmitter transmitter) {
        /* SERVER */
        transmitter.registerListener(RequestPacket.class, (channel, packet) -> {
            if (responders.containsKey(packet.id())) {
                this.pending.put(packet.uniqueId(), new PendingRequest(channel, packet.id(), System.currentTimeMillis()));

                var responders = this.responders.get(packet.id());
                var rndm = RandomUtils.getRandomNumber(responders.size());

                transmitter.send(channel, new RequestForwardPacket(rndm, packet.id(), packet.uniqueId(), packet.document()));

                NextCluster.LOGGER.info("Request received: " + packet.id() + " - responder: " + rndm + " - properties: " + packet.document());
            } else {
                channel.writeAndFlush(new BadResponsePacket(packet.id(), packet.uniqueId(), "No responder registered for id '" + packet.id() + "'"));
            }
        });
        transmitter.registerListener(ResponderRegistrationPacket.class, (channel, packet) -> {
            if (!responders.containsKey(packet.id())) {
                this.responders.put(packet.id(), Lists.newArrayList());
            }

            this.responders.get(packet.id()).add(channel);
            this.respondersByChannel.put(channel, packet.id());

            NextCluster.LOGGER.info("Registered responder: " + packet.id());
        });

        /* CLIENT */
        transmitter.registerListener(BadResponsePacket.class, (channel, packet) -> {
            if (transmitter.isRequestPresent(packet.uniqueId())) {
                NextCluster.LOGGER.error("Bad response received for request '" + packet.uniqueId() + "': " + packet.message());
                transmitter.removeRequest(packet.uniqueId());
            }
        });
        transmitter.registerListener(RequestResponsePacket.class, (channel, packet) -> {
            if (transmitter.isRequestPresent(packet.uuid())) {
                transmitter.acceptRequests(packet.uuid(), packet.packet());
            }
        });
        transmitter.registerListener(RequestForwardPacket.class, (channel, packet) -> {
            if (transmitter.isResponderPresent(packet.id())) {
                channel.writeAndFlush(new RequestResponsePacket(packet.uniqueId(), transmitter.getResponder(packet.id()).response(channel, packet.document())));
            }
        });
    }

    public void unregisterChannel(Channel channel) {
        if (this.respondersByChannel.containsKey(channel)) {
            var key = this.respondersByChannel.get(channel);
            this.responders.remove(key);
            this.respondersByChannel.remove(channel);

            NextCluster.LOGGER.info("Unregistered responder: " + key);
        }
    }
}