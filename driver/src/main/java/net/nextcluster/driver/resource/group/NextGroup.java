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

package net.nextcluster.driver.resource.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.Preconditions;
import io.fabric8.generator.annotation.Default;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import lombok.*;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.ClusterResource;
import net.nextcluster.driver.resource.Platform;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Kind("Group")
@Version("v1")
@Group("nextcluster.net")
@Getter
public class NextGroup extends ClusterResource<NextGroup.Spec, NextGroup.Status> implements ClusterGroup {

    @Override
    public String name() {
        return this.getMetadata().getName();
    }

    @Override
    public String image() {
        return this.getSpec().getBase().getImage();
    }

    @Override
    @JsonIgnore
    public boolean isMaintenance() {
        return this.getSpec().isMaintenance();
    }

    @Override
    @JsonIgnore
    public boolean isFallback() {
        return this.getSpec().isFallback();
    }

    @Override
    @JsonIgnore
    public boolean isStatic() {
        return this.getSpec().isStatic();
    }

    @Override
    public int minOnline() {
        return this.getSpec().getMinOnline();
    }

    @Override
    public int maxOnline() {
        return this.getSpec().getMaxOnline();
    }

    @Override
    public long maxMemory() {
        return this.getSpec().getMaxMemory();
    }

    @Override
    public Platform platform() {
        return Platform.valueOf(this.environment().getOrDefault("PLATFORM", "CUSTOM"));
    }

    @Override
    public Map<String, String> environment() {
        return Arrays.stream(getSpec().getBase().environment)
            .map(env -> env.split("="))
            .collect(Collectors.toMap(env -> env[0], env -> env[1]));
    }

    @Override
    public void shutdown() {
        for (Pod pod : NextCluster.instance().kubernetes().pods().withLabel("nextcluster/group", this.name())
            .list().getItems()
        ) {
            NextCluster.instance().kubernetes().pods().withName(pod.getMetadata().getName()).delete();
        }
    }

    @Override
    public Builder asBuilder() {
        return new Builder(this);
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Spec {
        @JsonPropertyDescription("The base configuration for the group")
        private Base base;

        @Default("false")
        @JsonPropertyDescription("Status if players can join the group")
        private boolean maintenance;

        @Default("1")
        @JsonPropertyDescription("The minimum amount of online servers for the group")
        private int minOnline;

        @Default("-1")
        @JsonPropertyDescription("The maximum amount of online servers for the group")
        private int maxOnline;

        @Default("512")
        @JsonPropertyDescription("The maximum amount of memory for the group in megabytes")
        private Long maxMemory;

        @Default("false")
        @JsonPropertyDescription("Status if the group is a fallback group (unnecessary for proxies)")
        private boolean fallback;

        @Default("false")
        @JsonProperty(value = "static")
        private boolean isStatic;

        @Getter
        @Setter(AccessLevel.PACKAGE)
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Base {
            private String image;
            private ClusterPort[] ports;
            private ClusterVolume[] volumes;
            private String[] environment;
            private String[] imagePullSecrets;
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ClusterPort {
            private String name;
            private Integer port;
            private Integer expose;
            private String protocol;

            public ContainerPort toContainerPort() {
                return new ContainerPortBuilder()
                    .withName(this.name)
                    .withContainerPort(this.port)
                    .withProtocol(this.protocol)
                    .build();
            }
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ClusterVolume {
            private String name;
            private String host;
            private String container;

            public Volume toVolume() {
                // @formatter:off
                return new VolumeBuilder()
                    .withName(this.name)
                        .withNewHostPath()
                            .withPath(this.host)
                        .endHostPath()
                    .build();
                // @formatter:on
            }

            public VolumeMount toMount() {
                return new VolumeMountBuilder()
                    .withName(this.name)
                    .withMountPath(this.container)
                    .build();
            }
        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        @JsonPropertyDescription("The amount of players currently online on this group")
        private int players;
    }

    public static class Builder {

        private final NextGroup group;
        private final Spec spec;

        private Builder(NextGroup group) {
            this.group = group;
            this.spec = group.getSpec();
        }

        Builder(String name) {
            this.group = new NextGroup();
            this.spec = new Spec();

            this.group.setMetadata(new ObjectMetaBuilder()
                .withName(name)
                .withNamespace(NextCluster.instance().kubernetes().getNamespace())
                .build()
            );
            this.spec.setBase(new Spec.Base());
            this.group.setStatus(new Status());
        }

        public Builder withImage(String value) {
            this.spec.getBase().setImage(value);
            return this;
        }

        public Builder withMinOnline(int value) {
            this.spec.setMinOnline(value);
            return this;
        }

        public Builder withMaxOnline(int value) {
            this.spec.setMaxOnline(value);
            return this;
        }

        public Builder withFallback(boolean value) {
            this.spec.setFallback(value);
            return this;
        }

        public Builder withMaintenance(boolean value) {
            this.spec.setMaintenance(value);
            return this;
        }
        public Builder withPlatform(Platform platform) {
            group.environment().put("PLATFORM", platform.name());
            return this;
        }

        public Builder withStatic(boolean value) {
            this.spec.setStatic(value);
            return this;
        }

        public Builder withMaxMemory(long value) {
            this.spec.setMaxMemory(value);
            return this;
        }

        public void publish() {
            Preconditions.checkNotNull(this.spec.base.image, "Image is required");

            group.setSpec(spec);
            group.getMetadata().setManagedFields(null);

            NextCluster.LOGGER.info(Serialization.asYaml(group));

            NextCluster.instance()
                .kubernetes()
                .resources(NextGroup.class)
                .resource(group)
                .forceConflicts()
                .serverSideApply();
        }
    }
}