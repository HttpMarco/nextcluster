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

package net.nextcluster.manager.resources.group.processor;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.group.NextGroup;

import java.util.List;
import java.util.stream.Stream;

public final class DynamicGroupProcessor implements GroupProcessor {

    @Override
    public void deploy(NextGroup group) {
        final var client = NextCluster.instance().kubernetes();
        final var ports = Stream.of(group.getSpec().getBase().getPorts())
            .map(port -> new ContainerPortBuilder()
                .withContainerPort(port.getPort())
                .withProtocol(port.getProtocol())
                .build())
            .toList();
        final var volumes = List.of(group.getSpec().getBase().getVolumes());

        // @formatter:off
        final var deployment = new DeploymentBuilder()
            .withNewMetadata()
                .withName(group.getMetadata().getName())
                .withNamespace(group.getMetadata().getNamespace())
                .addToLabels("nextcluster", "true")
            .endMetadata()
            .withNewSpec()
                .withReplicas(group.minOnline())
                .withSelector(
                    new LabelSelectorBuilder()
                        .addToMatchLabels("nextcluster/group", group.name())
                        .build()
                )
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("nextcluster", "true")
                        .addToLabels("nextcluster/group", group.name())
                        .addToLabels("nextcluster/fallback", String.valueOf(group.isFallback()))
                        .addToLabels("nextcluster/type", group.platform().type())
                        .addToLabels("nextcluster/static", group.getSpec().isStatic() ? "true" : "false")
                    .endMetadata()
                    .withNewSpec()
                        .withServiceAccountName("nextcluster")
                        .addNewContainer()
                            .withName("server")
                            .withImage(group.image())
                            .withTty()
                            .withStdin()
                            .withImagePullPolicy("Always")
                            .addAllToPorts(ports)
                            .addAllToEnv(
                                group.environment().entrySet().stream().map(entry -> new EnvVarBuilder()
                                    .withName(entry.getKey())
                                    .withValue(entry.getValue())
                                    .build())
                                    .toList()
                            )
                            .addAllToVolumeMounts(volumes.stream().map(NextGroup.Spec.ClusterVolume::toMount).toList())
                            .withNewResources()
                                .addToLimits("memory", new Quantity(group.maxMemory() + "Mi"))
                            .endResources()
                        .endContainer()
                        .addAllToVolumes(volumes.stream().map(NextGroup.Spec.ClusterVolume::toVolume).toList())
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();
        // @formatter:on
        client.apps().deployments().resource(deployment).serverSideApply();
    }
}
