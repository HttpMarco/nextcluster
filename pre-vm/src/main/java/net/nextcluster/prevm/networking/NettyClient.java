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

import io.netty5.bootstrap.Bootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.EventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.networking.NetworkChannelInitializer;
import net.nextcluster.driver.networking.NetworkUtils;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class NettyClient {

    @Getter
    @Accessors(fluent = true)
    private final NettyClientTransmitter transmitter;
    private final EventLoopGroup eventLoopGroup = NetworkUtils.createEventLoopGroup(1);

    public CompletableFuture<Void> connect(String hostname, int port) {
        var future = new CompletableFuture<Void>();
        new Bootstrap()
                .group(eventLoopGroup)
                .channelFactory(NetworkUtils::createChannelFactory)
                .handler(new NetworkChannelInitializer(new NettyClientHandler(this.transmitter)))
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .connect(hostname, port)
                .addListener(it -> {
                    if (it.isSuccess()) {
                        NextCluster.LOGGER.info("Successfully connected to controller ({})", port);
                        future.complete(null);
                    } else {
                        NextCluster.LOGGER.error("Controller connection failed: {}", it.cause().getMessage());
                        future.completeExceptionally(it.cause());
                    }
                });
        future.exceptionally(throwable -> {
            NextCluster.LOGGER.error("Controller connection failed: {}", throwable.getMessage());
            return null;
        });
        return future;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
