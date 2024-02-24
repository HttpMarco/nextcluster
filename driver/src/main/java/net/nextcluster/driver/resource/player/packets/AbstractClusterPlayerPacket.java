package net.nextcluster.driver.resource.player.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.networking.packets.ByteBuffer;
import net.nextcluster.driver.networking.packets.ClusterPacket;
import net.nextcluster.driver.resource.player.ClusterPlayer;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class AbstractClusterPlayerPacket implements ClusterPacket {

    private ClusterPlayer clusterPlayer;

    @Override
    public void write(ByteBuffer buffer) {
        buffer.writeBoolean(clusterPlayer != null);

        if(clusterPlayer != null) {
            buffer.writeString(clusterPlayer.name());
            buffer.writeUUID(clusterPlayer.uniqueId());
            buffer.writeString(clusterPlayer.connectedProxyName());
            buffer.writeString(clusterPlayer.connectedServerName());
        }
    }

    @Override
    public void read(ByteBuffer buffer) {
        var state = buffer.readBoolean();

        if(!state) {
            return;
        }

        var name = buffer.readString();
        var uuid = buffer.readUUID();
        var currentProxyName = buffer.readString();
        var currentServerName = buffer.readString();

        this.clusterPlayer = NextCluster.instance().playerProvider().createPlayer(name, uuid, currentProxyName, currentServerName);
    }
}
