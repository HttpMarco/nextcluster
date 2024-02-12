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

import io.netty5.bootstrap.ServerBootstrap;
import io.netty5.channel.*;
import io.netty5.channel.epoll.Epoll;
import io.netty5.channel.epoll.EpollServerSocketChannel;
import io.netty5.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.networking.NetworkChannelInitializer;
import net.nextcluster.driver.networking.NetworkUtils;

public final class NettyServer implements AutoCloseable {

    @Getter
    @Accessors(fluent = true)
    private final MultithreadEventLoopGroup bossGroup = NetworkUtils.createEventLoopGroup(1);
    private final MultithreadEventLoopGroup workerGroup = NetworkUtils.createEventLoopGroup(1);

    public void initialize(int port) {
        NetworkTrackingDetector.detect();
        new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channelFactory(generateChannelFactory())
                .childHandler(new NetworkChannelInitializer(new NettyServerHandler()))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind("0.0.0.0", port)
                .addListener(it -> {
                    if (it.isSuccess()) {
                        NextCluster.LOGGER.info("Server started on port " + port);
                        it.getNow().closeFuture();
                    } else {
                        NextCluster.LOGGER.info("Failed to start on port " + port + " " + it.cause().getMessage());
                    }
                });
    }

    @Override
    public void close() throws Exception {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private ServerChannelFactory<? extends ServerChannel> generateChannelFactory() {
        return Epoll.isAvailable() ? EpollServerSocketChannel::new : NioServerSocketChannel::new;
    }
}
