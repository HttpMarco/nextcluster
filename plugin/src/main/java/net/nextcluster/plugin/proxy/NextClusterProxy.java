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

package net.nextcluster.plugin.proxy;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.player.ClusterPlayer;
import net.nextcluster.plugin.NextClusterPlugin;
import net.nextcluster.plugin.resources.player.ClientClusterPlayerProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class NextClusterProxy extends NextClusterPlugin {

    protected final Map<String, InternalClusterServer> servers = new ConcurrentHashMap<>();

    public void watch() {
        init();

        NextCluster.instance().playerProvider(new ClientClusterPlayerProvider());
        NextCluster.instance()
            .kubernetes()
            .pods()
            .withLabel("nextcluster", "true")
            .withLabel("nextcluster/type", "SERVER")
                .inform(new ResourceEventHandler<>() {
                    @Override
                    public void onAdd(Pod obj) {
                        registerServer(obj);
                    }

                    @Override
                    public void onUpdate(Pod oldObj, Pod newObj) {
                        registerServer(newObj);
                    }

                    @Override
                    public void onDelete(Pod obj, boolean deletedFinalStateUnknown) {
                        final var name = obj.getMetadata().getName();
                        if (!servers.containsKey(name)) {
                            return;
                        }
                        unregisterServer0(servers.remove(name));
                    }
                });
    }

    public abstract void registerServer(InternalClusterServer server);

    public abstract ClusterPlayer buildPlayer(UUID uniqueId, String currentProxyName);

    private void registerServer0(InternalClusterServer server) {
        NextCluster.LOGGER.info("Server {} successfully connected to the cluster", server.name());

        servers.put(server.name(), server);
        registerServer(server);
    }

    public abstract void unregisterServer(InternalClusterServer server);

    private void unregisterServer0(InternalClusterServer server) {
        NextCluster.LOGGER.info("Server {} is now unregistered", server.name());

        servers.remove(server.name());
        unregisterServer(server);
    }

    private void registerServer(Pod pod) {
        final var name = pod.getMetadata().getName();
        if (servers.containsKey(name)) {
            return;
        }
        final var ip = pod.getStatus().getPodIP();
        if (ip == null) {
            return;
        }
        final var group = pod.getMetadata().getLabels().get("nextcluster/group");
        if (group == null) {
            return;
        }
        final var fallback = Boolean.parseBoolean(
                pod.getMetadata().getLabels()
                        .getOrDefault("nextcluster/fallback", "false")
        );
        registerServer0(new InternalClusterServer(name, group, fallback, ip));
    }
}
