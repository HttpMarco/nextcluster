package net.nextcluster.plugin.proxy.waterdogpe;

import dev.httpmarco.osgan.files.annotations.ConfigExclude;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import net.nextcluster.driver.resource.player.AbstractClusterPlayer;

public final class WaterdogPEClusterPlayer extends AbstractClusterPlayer {

    @ConfigExclude
    private final ProxiedPlayer player;

    public WaterdogPEClusterPlayer(ProxiedPlayer player, String currentProxyName) {
        super(player.getName(), player.getUniqueId(), currentProxyName, player.getServerInfo().getServerName());
        this.player = player;
    }

    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(message);
    }

    @Override
    public void sendActionBar(String message) {
        this.player.sendTip(message);
    }

    public void sendToastMessage(String title, String content) {
        this.player.sendToastMessage(title, content);
    }

    @Override
    public void sendTitle(String title, String subtitle, Integer fadeIn, Integer stay, Integer fadeOut) {
        this.player.sendTitle(
                title,
                subtitle,
                fadeIn,
                stay,
                fadeOut
        );
    }

    @Override
    public void kick(String reason) {
        this.player.disconnect(reason);
    }

    @Override
    public void connectToServer(String serverName) {
        this.player.connect(ProxyServer.getInstance().getServerInfo(serverName));
    }
}
