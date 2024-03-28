package net.nextcluster.plugin.server.minestom;

import dev.httpmarco.osgan.utils.data.Pair;
import net.minestom.server.MinecraftServer;
import net.minestom.server.plugins.Plugin;
import net.nextcluster.driver.resource.service.ServiceInformation;
import net.nextcluster.plugin.NextClusterPlugin;

public class MinestomPlugin extends Plugin {


    @Override
    public void onEnable() {
        new ClusterPlugin();
    }

    @Override
    public void onDisable() {

    }

    private static class ClusterPlugin extends NextClusterPlugin {

        public ClusterPlugin() {
            init();
        }

        @Override
        public ServiceInformation currentInformation() {
            return new ServiceInformation(
                    MinecraftServer.getConnectionManager().getOnlinePlayers().size(),
                    -1,
                    "null",
                    MinecraftServer.getConnectionManager().getOnlinePlayers()
                            .stream()
                            .map(player -> new Pair<>(player.getUuid(), player.getUsername()))
                            .toList()
            );
        }

        @Override
        public void dispatchCommand(String command) {
            //todo
        }
    }

}
