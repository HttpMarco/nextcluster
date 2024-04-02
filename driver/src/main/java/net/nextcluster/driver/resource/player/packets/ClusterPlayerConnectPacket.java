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
public final class ClusterPlayerConnectPacket extends Packet {
     private final @Nullable ClusterPlayer clusterPlayer;

     public ClusterPlayerConnectPacket(@Nullable ClusterPlayer clusterPlayer) {
          super();

          this.clusterPlayer = clusterPlayer;

          this.getBuffer().writeObject(clusterPlayer, buffer -> Objects.requireNonNull(clusterPlayer).write(buffer));
     }

     public ClusterPlayerConnectPacket(CodecBuffer buffer) {
          super(buffer);

          this.clusterPlayer = buffer.readObject(ClusterPlayer.class, () -> new DefaultClusterPlayer(
                  buffer.readString(),
                  buffer.readUniqueId(),
                  buffer.readString(),
                  buffer.readString()
          ));
     }
}
