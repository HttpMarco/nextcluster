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

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Default;
import io.fabric8.kubernetes.api.model.*;
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
    public boolean isMaintenance() {
        return this.getSpec().isMaintenance();
    }

    @Override
    public boolean isFallback() {
        return this.getSpec().isFallback();
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

    @Getter
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

        @Default("100")
        @JsonPropertyDescription("The maximum amount of online servers for the group")
        private int maxOnline;

        @Default("512")
        @JsonPropertyDescription("The maximum amount of memory for the group in megabytes")
        private Long maxMemory;

        @Default("false")
        @JsonPropertyDescription("Status if the group is a fallback group (unnecessary for proxies)")
        private boolean fallback;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Base {
            private String image;
            private ClusterPort[] ports;
            private ClusterVolume[] volumes;
            private String[] environment;
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ClusterPort {
            private String name;
            private Integer port;
            private Integer expose;
            private String protocol;
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

    // TODO: Improve
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Builder {

        private final String name;

        public void publish() {
            final var group = new NextGroup();
            group.setMetadata(new ObjectMetaBuilder().withName(this.name).build());
            NextCluster.instance().kubernetes().resources(NextGroup.class).resource(group).serverSideApply();
        }

    }

}
