package net.nextcluster.plugin.proxy.bungeecord;

import dev.httpmarco.osgon.configuration.ConfigExclude;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.nextcluster.driver.resource.player.AbstractClusterPlayer;

public final class BungeeCordClusterPlayer extends AbstractClusterPlayer {

    @ConfigExclude
    private final ProxiedPlayer player;

    public BungeeCordClusterPlayer(ProxiedPlayer player, String currentProxyName) {
        super(player.getName(), player.getUniqueId(), currentProxyName, player.getServer().getInfo().getName());
        this.player = player;
    }

    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(TextComponent.fromLegacy(message));
    }

    @Override
    public void sendActionBar(String message) {
        throw new UnsupportedOperationException("Bungeecord does not support action bars");
    }

    @Override
    public void sendTitle(String title, String subtitle, Integer fadeIn, Integer stay, Integer fadeOut) {
        this.player.sendTitle(ProxyServer.getInstance().createTitle()
                .title(TextComponent.fromLegacy(title))
                .subTitle(TextComponent.fromLegacy(subtitle))
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut));
    }

    @Override
    public void kick(String reason) {
        this.player.disconnect(TextComponent.fromLegacy(reason));
    }

    @Override
    public void connectToServer(String serverName) {
        this.player.connect(ProxyServer.getInstance().getServerInfo(serverName));
    }
}
