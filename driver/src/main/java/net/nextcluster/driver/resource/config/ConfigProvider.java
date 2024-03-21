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
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import lombok.RequiredArgsConstructor;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.config.misc.ConfigProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ConfigProvider {

    private static final Map<String, NextConfig<?>> CONFIGS = new ConcurrentHashMap<>();

    private Watch watch;

    public <T> NextConfig<T> register(NextConfig<T> config) {
        CONFIGS.put(config.name(), config);

        var configMap = NextCluster.instance().kubernetes().configMaps().withName(config.name()).get();

        if (configMap != null) {
            final var value = configMap.getData().get("value");
            if (value != null) {
                config.value(JsonUtils.fromJson(value, config.type()));
            }
        } else {
            // @formatter:off
            configMap = new ConfigMapBuilder()
                    .withNewMetadata()
                        .withName(config.name())
                        .withNamespace(NextCluster.instance().kubernetes().getNamespace())
                    .endMetadata()
                    .withData(Map.of("value", JsonUtils.toPrettyJson(config.value())))
                    .build();
            // @formatter:on
            NextCluster.instance().kubernetes().configMaps().resource(configMap).serverSideApply();
        }

        if (this.watch == null && config.hasProperty(ConfigProperty.OBSERVE)) {
            this.watch = NextCluster.instance().kubernetes().configMaps().watch(new Watcher<>() {
                @Override
                public void eventReceived(Action action, ConfigMap resource) {
                    if (action == Action.DELETED) {
                        CONFIGS.remove(resource.getMetadata().getName());
                        return;
                    }
                    final var config = CONFIGS.get(resource.getMetadata().getName());
                    if (config == null) {
                        return;
                    }
                    final var value = resource.getData().get("value");
                    if (value == null) {
                        config.value(null);
                        return;
                    }
                    config.valueMapping(JsonUtils.fromJson(value, config.type()));
                }

                @Override
                public void onClose(WatcherException cause) {
                    cause.printStackTrace(System.err);
                }
            });
        }
        return config;
    }
}
