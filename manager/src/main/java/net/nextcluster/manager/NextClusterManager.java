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

package net.nextcluster.manager;

import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.osgan.networking.server.NettyServer;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.event.ClusterEvent;
import net.nextcluster.driver.event.ClusterEventCallPacket;
import net.nextcluster.driver.resource.group.NextGroup;
import net.nextcluster.driver.transmitter.ChannelIdPacket;
import net.nextcluster.driver.transmitter.NetworkTransmitter;
import net.nextcluster.driver.transmitter.RedirectPacket;
import net.nextcluster.manager.networking.NettyServerTransmitter;
import net.nextcluster.manager.resources.group.NextGroupWatcher;
import net.nextcluster.manager.resources.player.ManagerCloudPlayerProvider;

import java.util.function.Supplier;

@Getter
@Accessors(fluent = true)
public class NextClusterManager extends NextCluster {

    public static final Supplier<String> STATIC_SERVICES_PATH = () ->
            "/srv/nextcluster/%s/static".formatted(NextCluster.instance().kubernetes().getNamespace());

    private final NettyServer nettyServer;

    protected NextClusterManager() {
        // register communication transmitter (priority)
        super(new NettyServerTransmitter());

        // initialize netty server
        this.nettyServer = NettyServer.builder()
                .withPort(NetworkTransmitter.NETTY_PORT)
                .onInactive(transmit -> ((NettyServerTransmitter) transmitter()).unregisterTransmitter(transmit))
                .build();

        // wait for the transmitter to be ready
        playerProvider(new ManagerCloudPlayerProvider(this.transmitter()));

        transmitter().listen(ClusterEventCallPacket.class, (channel, packet) -> {
            nettyServer.sendPacketAndIgnoreSelf(channel.channel(), packet);

            try {
                NextCluster.instance().eventRegistry().callLocal(JsonUtils.fromJson(packet.json(), (Class<? extends ClusterEvent>) Class.forName(packet.eventClass())));
                NextCluster.LOGGER.info("Calling cluster event: " + packet.eventClass());
            } catch (ClassNotFoundException ignored) {
            }
        });

        transmitter().listen(RedirectPacket.class, (transmit, packet) -> ((NettyServerTransmitter) transmitter()).doRedirect(packet.id(), packet));

        transmitter().listen(ChannelIdPacket.class, (transmit, packet) -> ((NettyServerTransmitter) transmitter()).registerTransmitter(packet.id(), transmit));
    }

    public static void main(String[] args) {
        long startup = System.currentTimeMillis();
        new NextClusterManager();

        var client = NextCluster.instance().kubernetes();
        LOGGER.info("Applying custom resources...");
        Initializer.initialize(client);
        client.apiextensions()
                .v1()
                .customResourceDefinitions()
                .load(ClassLoader.getSystemClassLoader().getResourceAsStream("models/nextgroup.yml"))
                .forceConflicts()
                .serverSideApply();
        LOGGER.info("Custom resources successfully applied!");
        client.resources(NextGroup.class).inform(new NextGroupWatcher());
        LOGGER.info("NextClusterManager started in {}ms!", System.currentTimeMillis() - startup);
    }
}