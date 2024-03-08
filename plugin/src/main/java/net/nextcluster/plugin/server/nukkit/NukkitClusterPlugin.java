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

package net.nextcluster.plugin.server.nukkit;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import dev.httpmarco.osgan.utils.data.Pair;
import net.nextcluster.driver.resource.service.ServiceInformation;
import net.nextcluster.plugin.NextClusterPlugin;

public class NukkitClusterPlugin extends PluginBase {

    @Override
    public void onEnable() {
        new ClusterPlugin();
    }

    private static class ClusterPlugin extends NextClusterPlugin {
        public ClusterPlugin() {
            init();
        }

        @Override
        public ServiceInformation currentInformation() {
            return new ServiceInformation(
                    Server.getInstance().getOnlinePlayers().size(),
                    Server.getInstance().getMaxPlayers(),
                    Server.getInstance().getMotd(),
                    Server.getInstance().getOnlinePlayers().entrySet()
                            .stream()
                            .map(player -> new Pair<>(player.getKey(), player.getValue().getName()))
                            .toList()
            );
        }

        @Override
        public void dispatchCommand(String command) {
            Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), command);
        }
    }
}