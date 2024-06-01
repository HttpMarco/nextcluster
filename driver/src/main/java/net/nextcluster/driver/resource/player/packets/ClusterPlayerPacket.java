package net.nextcluster.driver.resource.player.packets;

import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.codec.CodecBuffer;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.player.DefaultClusterPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
@Accessors(fluent = true)
public class ClusterPlayerPacket extends Packet {
    private final @Nullable DefaultClusterPlayer clusterPlayer;

    public ClusterPlayerPacket(@Nullable DefaultClusterPlayer clusterPlayer) {
        super();

        this.clusterPlayer = clusterPlayer;

        this.getBuffer().writeObject(clusterPlayer, buffer -> Objects.requireNonNull(clusterPlayer).write(buffer));
    }

    public ClusterPlayerPacket(CodecBuffer buffer) {
        super(buffer);

        this.clusterPlayer = buffer.readObject(DefaultClusterPlayer.class, () -> new DefaultClusterPlayer(
                buffer.readString(),
                buffer.readUniqueId(),
                buffer.readString(),
                buffer.readString()
        ));
    }
}
