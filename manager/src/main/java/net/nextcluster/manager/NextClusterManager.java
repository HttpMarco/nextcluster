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

import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.event.ClusterEventCallPacket;
import net.nextcluster.driver.networking.NetworkUtils;
import net.nextcluster.driver.resource.group.NextGroup;
import net.nextcluster.manager.networking.NettyServer;
import net.nextcluster.manager.networking.NettyServerTransmitter;
import net.nextcluster.manager.resources.group.NextGroupWatcher;
import net.nextcluster.manager.resources.player.ManagerCloudPlayerProvider;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.function.Supplier;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class NextClusterManager extends NextCluster {

    public static final Supplier<String> STATIC_SERVICES_PATH = () ->
        "/srv/nextcluster/%s/static".formatted(NextCluster.instance().kubernetes().getNamespace());

    protected NextClusterManager() {
        // register communication transmitter (priority)
        super(new NettyServerTransmitter());

        // initialize netty server
        var nettyServer = new NettyServer();
        nettyServer.initialize(NetworkUtils.NETTY_PORT);

        // wait for the transmitter to be ready
        playerProvider(new ManagerCloudPlayerProvider(this.transmitter()));
    }

    public static void main(String[] args) {
        long startup = System.currentTimeMillis();
        new SpringApplicationBuilder(NextClusterManager.class)
            .bannerMode(Banner.Mode.OFF)
            .run(args);

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