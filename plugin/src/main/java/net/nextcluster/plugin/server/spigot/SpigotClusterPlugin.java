/*
 * MIT License
 *
 * Copyright (c) 2024 nextCluster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.nextcluster.plugin.server.spigot;

import dev.httpmarco.osgan.utils.data.Pair;
import net.kyori.adventure.text.TextComponent;
import net.nextcluster.driver.resource.service.ServiceInformation;
import net.nextcluster.plugin.NextClusterPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpigotClusterPlugin extends JavaPlugin {

    private static SpigotClusterPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        new ClusterPlugin();
    }

    private static class ClusterPlugin extends NextClusterPlugin {

        public ClusterPlugin() {
            init();
        }

        @Override
        public ServiceInformation currentInformation() {
            return new ServiceInformation(
                Bukkit.getOnlinePlayers().size(),
                Bukkit.getMaxPlayers(),
                ((TextComponent) Bukkit.motd()).content(),
                Bukkit.getOnlinePlayers()
                    .stream()
                    .map(player -> new Pair<>(player.getUniqueId(), player.getName()))
                    .toList()
            );
        }

        @Override
        public void dispatchCommand(String command) {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

}
