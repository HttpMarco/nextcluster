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
import net.nextcluster.driver.NextCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NextClusterProxy {

    protected final Map<String, InternalClusterServer> servers = new ConcurrentHashMap<>();

    public void watch() {
        NextCluster.instance()
            .kubernetes()
            .pods()
            .withLabel("nextcluster", "true")
            .withLabel("nextcluster/type", "SERVER")
            .watch(new Watcher<>() {
                @Override
                public void eventReceived(Action action, Pod resource) {
                    final var name = resource.getMetadata().getName();
                    switch (action) {
                        case ADDED, MODIFIED -> {
                            if (servers.containsKey(name)) {
                                return;
                            }
                            final var ip = resource.getStatus().getPodIP();
                            if (ip == null) {
                                return;
                            }
                            final var group = resource.getMetadata().getLabels().get("nextcluster/group");
                            if (group == null) {
                                return;
                            }
                            final var fallback = Boolean.parseBoolean(
                                resource.getMetadata().getLabels()
                                    .getOrDefault("nextcluster/fallback", "false")
                            );
                            registerServer0(new InternalClusterServer(name, group, fallback, ip));
                        }
                        case DELETED -> {
                            if (!servers.containsKey(name)) {
                                return;
                            }
                            unregisterServer0(servers.remove(name));
                        }
                    }
                }

                @Override
                public void onClose(WatcherException cause) {
                    cause.printStackTrace(System.err);
                }
            });
    }

    public abstract void registerServer(InternalClusterServer server);

    private void registerServer0(InternalClusterServer server) {
        NextCluster.LOGGER.info("Registering server: " + server.name());

        servers.put(server.name(), server);
        registerServer(server);
    }

    public abstract void unregisterServer(InternalClusterServer server);

    private void unregisterServer0(InternalClusterServer server) {
        NextCluster.LOGGER.info("Unregistering server: " + server.name());

        servers.remove(server.name());
        unregisterServer(server);
    }

}
