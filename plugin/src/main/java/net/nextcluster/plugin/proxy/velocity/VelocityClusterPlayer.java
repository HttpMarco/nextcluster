package net.nextcluster.plugin.proxy.velocity;

import com.velocitypowered.api.proxy.Player;
import dev.httpmarco.osgon.configuration.ConfigExclude;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.nextcluster.driver.resource.player.AbstractClusterPlayer;

import java.time.Duration;

public class VelocityClusterPlayer extends AbstractClusterPlayer {

    @ConfigExclude
    private final Player player;

    public VelocityClusterPlayer(Player player) {
        super(player.getUsername(), player.getUniqueId(), System.getenv("HOSTNAME"), null);
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
        // TODO FIND A WAY TO DO THIS
       // this.player.createConnectionRequest(Prox).fireAndForget();
    }
}
