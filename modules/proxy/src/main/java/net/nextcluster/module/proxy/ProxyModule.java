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

package net.nextcluster.module.proxy;

import net.nextcluster.driver.resource.config.NextConfig;
import net.nextcluster.driver.resource.config.misc.ConfigProperty;
import net.nextcluster.module.proxy.config.ProxyConfiguration;
import net.nextcluster.module.proxy.config.models.MotdModel;

import java.util.ArrayList;
import java.util.List;

public abstract class ProxyModule {

    private final NextConfig<ProxyConfiguration> config = NextConfig.builder(ProxyConfiguration.class)
        .withId("proxy-module-config")
        .withProperties(ConfigProperty.OBSERVE, ConfigProperty.UPDATE_ORIGINAL)
        .register();

    public void initialize() {
        if (!config.exists()) {
            final ProxyConfiguration configuration = new ProxyConfiguration(
                new ArrayList<>(List.of(new MotdModel(
                    "§6NextCluster §7- §eNextCluster Network",
                    "§7Join our Discord at §ehttps://discord.gg/nextcluster",
                    3 * 20L
                ))),
                new ArrayList<>(List.of(new MotdModel(
                    "§6NextCluster §7- §eNextCluster Network",
                    "§7Join our Discord at §ehttps://discord.gg/nextcluster",
                    3 * 20L
                ))),
                new ArrayList<>(),
                new ArrayList<>()
            );
            config.value(configuration);
        }
    }

}
