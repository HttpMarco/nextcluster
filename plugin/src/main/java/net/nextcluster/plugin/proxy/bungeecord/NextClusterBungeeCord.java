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

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;


@SuppressWarnings("unused")
public class NextClusterBungeeCord extends Plugin {

    private final BungeeCordProxy proxy = new BungeeCordProxy();

    @Override
    public void onEnable() {
        proxy.watch();
    }

    @EventHandler
    public void handle(ServerConnectEvent event) {
        proxy.findFallback().ifPresentOrElse(event::setTarget, () -> {
            event.getPlayer().disconnect("No fallback server available");
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        proxy.findFallback().ifPresentOrElse(event::setCancelServer, () -> {
            event.getPlayer().sendMessage(event.getReason());
            event.setCancelled(true);
        });
    }
}
