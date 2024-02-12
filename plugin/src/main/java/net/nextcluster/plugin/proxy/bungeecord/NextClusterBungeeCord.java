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

package net.nextcluster.plugin.proxy.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import net.nextcluster.plugin.proxy.NextClusterProxy;
import net.nextcluster.plugin.proxy.InternalClusterServer;

import java.net.InetSocketAddress;


@SuppressWarnings("unused")
public class NextClusterBungeeCord extends Plugin {

    @Override
    public void onEnable() {
        new NextClusterProxy() {
            @Override
            public void registerServer(InternalClusterServer server) {
                final var info = getProxy().constructServerInfo(
                    server.name(),
                    new InetSocketAddress(server.ip(), 25565),
                    "NextCluster Service",
                    false
                );
                getProxy().getServers().put(server.name(), info);
            }

            @Override
            public void unregisterServer(InternalClusterServer server) {
                getProxy().getServers().remove(server.name());
            }
        }.watch();
    }
}
