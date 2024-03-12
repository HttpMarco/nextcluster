package net.nextcluster.driver.networking.request;

import io.netty5.channel.Channel;

public record PendingRequest(Channel channel, String id, long timestamp) {
}
