package net.nextcluster.driver.resource.player.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.networking.packets.ByteBuffer;
import net.nextcluster.driver.networking.packets.ClusterPacket;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public final class ClusterPlayerResponsePacket implements ClusterPacket {

    private boolean online;

    @Override
    public void write(@NotNull ByteBuffer buffer) {
        buffer.writeBoolean(online);
    }

    @Override
    public void read(@NotNull ByteBuffer buffer) {
        this.online = buffer.readBoolean();
    }
}
