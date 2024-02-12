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

import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.networking.request.BadResponsePacket;
import net.nextcluster.driver.networking.request.RequestPacket;
import net.nextcluster.driver.networking.request.RequestResponsePacket;

public final class NetworkTransmitterDetector {

    public NetworkTransmitterDetector(NetworkTransmitter transmitter) {
        transmitter.registerListener(RequestPacket.class, (channel, packet) -> {
            if (transmitter.isResponderPresent(packet.id())) {
                channel.writeAndFlush(new RequestResponsePacket(packet.uniqueId(), transmitter.getResponder(packet.id()).response(channel, packet.document())));
            } else {
                // we send it back, but empty (throw exception on other side)
               channel.writeAndFlush(new BadResponsePacket(packet.id(), packet.uniqueId()));
            }
        });
        transmitter.registerListener(BadResponsePacket.class, (channel, packet) -> {
            if (transmitter.isRequestPresent(packet.uniqueId())) {
                NextCluster.LOGGER.error("Bad response received for request (responder not found): " + packet.uniqueId());
                transmitter.removeRequest(packet.uniqueId());
            }
        });
        transmitter.registerListener(RequestResponsePacket.class, (channel, packet) -> {
            if (transmitter.isRequestPresent(packet.uuid())) {
                transmitter.acceptRequests(packet.uuid(), packet.packet());
            }
        });
    }
}