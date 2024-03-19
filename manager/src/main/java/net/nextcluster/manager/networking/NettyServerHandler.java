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

import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.networking.packets.ClusterPacket;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public final class NettyServerHandler extends SimpleChannelInboundHandler<ClusterPacket> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ClusterPacket msg) {
        NextCluster.instance().transmitter().call(ctx.channel(), msg);
        NextCluster.LOGGER.debug("Received message: " + msg.getClass().getSimpleName() + " from " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ((NettyServerTransmitter) NextCluster.instance().transmitter()).channels().add(ctx.channel());
        NextCluster.LOGGER.debug("Channel active: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ((NettyServerTransmitter) NextCluster.instance().transmitter()).channels().remove(ctx.channel());
        NextCluster.LOGGER.debug("Channel inactive: " + ctx.channel().remoteAddress());
        NextCluster.instance().transmitter().unregisterChannel(ctx.channel());
    }

    @Override
    public void channelExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!cause.getMessage().equals("Connection reset")) cause.printStackTrace();
    }
}
