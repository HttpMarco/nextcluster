package net.nextcluster.plugin.proxy.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.httpmarco.osgan.files.annotations.ConfigExclude;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.player.AbstractClusterPlayer;
import net.nextcluster.driver.resource.player.DefaultClusterPlayer;
import net.nextcluster.plugin.NextClusterPlugin;

import java.time.Duration;

public class VelocityClusterPlayer extends DefaultClusterPlayer {

    @ConfigExclude
    private final Player player;

    public VelocityClusterPlayer(Player player) {
        super(player.getUsername(), player.getUniqueId(), System.getenv("HOSTNAME"), "");
        this.player = player;
    }

    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(Component.text(message));
    }

    @Override
    public void sendActionBar(String message) {
        this.player.sendActionBar(Component.text(message));
    }

    @Override
    public void sendTitle(String title, String subtitle, Integer fadeIn, Integer stay, Integer fadeOut) {
        this.player.showTitle(Title.title(Component.text(title), Component.text(subtitle),
                Title.Times.times(Duration.ofMillis(fadeIn),
                        Duration.ofMillis(stay),
                        Duration.ofMillis(fadeOut))));
    }

    @Override
    public void kick(String reason) {
        this.player.disconnect(Component.text(reason));
    }

    @Override
    public void connectToServer(String serverName) {
        ((NextClusterVelocity) NextClusterPlugin.instance()).getServer().getServer(serverName).ifPresent(registeredServer -> this.player.createConnectionRequest(registeredServer).connect());
    }
}
