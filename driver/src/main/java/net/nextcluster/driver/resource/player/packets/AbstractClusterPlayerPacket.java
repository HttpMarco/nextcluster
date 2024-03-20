package net.nextcluster.driver.resource.player.packets;

import dev.httpmarco.osgan.files.json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.networking.packets.ByteBuffer;
import net.nextcluster.driver.networking.packets.ClusterPacket;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.driver.resource.player.DefaultClusterPlayer;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class AbstractClusterPlayerPacket implements ClusterPacket {

    private String json;

    public AbstractClusterPlayerPacket(@Nullable ClusterPlayer clusterPlayer) {
        if (clusterPlayer != null) {
            this.json = JsonUtils.toJson(clusterPlayer);
        }
    }

    public ClusterPlayer clusterPlayer() {
        return (this.json != null ? JsonUtils.fromJson(this.json, DefaultClusterPlayer.class) : null);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.writeBoolean(json != null);

        if (json != null) {
            buffer.writeString(json);
        }
    }

    @Override
    public void read(ByteBuffer buffer) {
        var state = buffer.readBoolean();

        if (state) {
            this.json = buffer.readString();
        }
    }
}
