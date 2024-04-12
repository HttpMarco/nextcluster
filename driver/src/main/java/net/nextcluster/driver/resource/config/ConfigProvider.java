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

package net.nextcluster.driver.resource.config;

import dev.httpmarco.osgan.files.json.JsonUtils;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.config.misc.ConfigProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConfigProvider {

    private static final Map<String, NextConfig<?>> CONFIGS = new ConcurrentHashMap<>();

    public ConfigProvider() {
        NextCluster.instance().kubernetes().configMaps().inNamespace(NextCluster.instance().kubernetes().getNamespace()).inform(new ResourceEventHandler<>() {
            @Override
            public void onAdd(ConfigMap configMap) {
            }

            @Override
            public void onUpdate(ConfigMap configMap, ConfigMap t1) {
                if (!CONFIGS.containsKey(configMap.getMetadata().getName())) {
                    return;
                }

                var config = CONFIGS.get(configMap.getMetadata().getName());
                if (config == null || !config.hasProperty(ConfigProperty.OBSERVE)) {
                    return;
                }
                final var value = configMap.getData().get("value");
                if (value == null) {
                    config.value(null);
                    return;
                }
                config.valueMapping(JsonUtils.fromJson(value, config.type()));
            }

            @Override
            public void onDelete(ConfigMap configMap, boolean b) {
                CONFIGS.remove(configMap.getMetadata().getName());
            }
        }).start();
    }

    public <T> NextConfig<T> register(NextConfig<T> config) {
        CONFIGS.put(config.name(), config);

        var configMap = NextCluster.instance().kubernetes().configMaps().withName(config.name()).get();

        if (configMap != null) {
            final var value = configMap.getData().get("value");
            if (value != null) {
                config.value(JsonUtils.fromJson(value, config.type()));
            }
        } else {
            NextCluster.LOGGER.info("{} does not exist, creating...", config.name());
            config.value(config.value());
        }
        return config;
    }
}
