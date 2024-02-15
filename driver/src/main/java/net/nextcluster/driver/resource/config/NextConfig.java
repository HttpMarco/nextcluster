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

import com.google.common.base.Preconditions;
import dev.httpmarco.osgon.configuration.gson.JsonUtils;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.config.annotation.Config;
import net.nextcluster.driver.resource.config.misc.ConfigProperty;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class NextConfig<T> {

    private final Class<T> type;
    private final String name;
    private final Supplier<T> defaultValue;
    private final ConfigProperty[] properties;
    private T value;

    public static <T> Builder<T> builder(@NonNull Class<T> type) {
        return new Builder<>(type);
    }

    public boolean hasProperty(ConfigProperty property) {
        return properties != null && Arrays.stream(properties).anyMatch(p -> p == property);
    }

    @SuppressWarnings("unchecked")
    public void valueMapping(Object object) {
        this.value((T) object);
    }

    public void value(T value) {
        this.value = value;

        if (hasProperty(ConfigProperty.UPDATE_ORIGINAL)) {
            // @formatter:off
            var configMap = new ConfigMapBuilder()
                .withNewMetadata()
                    .withName(name)
                    .withNamespace(NextCluster.instance().kubernetes().getNamespace())
                .endMetadata()
                .withData(Map.of("value", JsonUtils.toPrettyJson(value)))
                .build();
            // @formatter:on
            NextCluster.instance().kubernetes().configMaps().resource(configMap).serverSideApply();
        }
    }

    public T value() {
        T t = this.value;
        if (t == null && defaultValue != null) {
            return defaultValue.get();
        }
        return t;
    }

    public boolean exists() {
        return NextCluster.instance().kubernetes().configMaps().withName(this.name).get() != null;
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Builder<T> {

        private final Class<T> type;
        private String id;
        private Supplier<T> defaultValue;
        private ConfigProperty[] properties;

        public Builder<T> withId(String value) {
            this.id = value;
            return this;
        }

        public Builder<T> withDefault(Supplier<T> value) {
            this.defaultValue = value;
            return this;
        }

        public Builder<T> withProperties(ConfigProperty... value) {
            this.properties = value;
            return this;
        }

        public NextConfig<T> build() {
            final String id = type.isAnnotationPresent(Config.class) ? type.getDeclaredAnnotation(Config.class).id() : this.id;

            Preconditions.checkNotNull(this.type, "type cannot be null");
            Preconditions.checkNotNull(id, "id cannot be null");

            return new NextConfig<>(this.type, id, this.defaultValue, this.properties);
        }

        public NextConfig<T> register() {
            return NextCluster.instance().configProvider().register(this.build());
        }

    }

}
