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

package net.nextcluster.module.proxy.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import net.nextcluster.module.proxy.ProxyModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin(
    id = "nextcluster-proxy",
    name = "NextCluster Proxy",
    version = "1.0.2",
    description = "A proxy module for the NextCluster network.",
    authors = {"nextCluster"},
    url = "https://wiki.nextcluster.net"
)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class VelocityProxyPlugin extends ProxyModule {

    public static final Logger LOGGER = LoggerFactory.getLogger(VelocityProxyPlugin.class);

    private final ProxyServer proxy;

    @SuppressWarnings("unused")
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        initialize();

        LOGGER.info("Proxy module loaded!");
    }

}
