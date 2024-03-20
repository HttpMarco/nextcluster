package net.nextcluster.driver.resource.player;

import dev.httpmarco.osgan.utils.exceptions.NotImplementedException;

import java.util.UUID;

public class DefaultClusterPlayer extends AbstractClusterPlayer {
    public DefaultClusterPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName) {
        super(name, uniqueId, currentProxyName, currentServerName);
    }

    @Override
    public void sendMessage(String message) {
        throw new NotImplementedException();
    }

    @Override
    public void sendActionBar(String message) {
        throw new NotImplementedException();
    }

    @Override
    public void sendTitle(String title, String subtitle, Integer fadeIn, Integer stay, Integer fadeOut) {
        throw new NotImplementedException();
    }

    @Override
    public void kick(String reason) {
        throw new NotImplementedException();
    }

    @Override
    public void connectToServer(String serverName) {
        throw new NotImplementedException();
    }
}
