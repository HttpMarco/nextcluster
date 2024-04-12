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

package net.nextcluster.manager.requirements;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Map;

public class Registry {

    public static void initialize(KubernetesClient client) {
        createPersistentVolume(client);
        createPersistentVolumeClaim(client);
        createDeployment(client);
        createService(client);
    }

    private static void createPersistentVolume(KubernetesClient client) {
        final var path = "/srv/nextcluster/%s/registry".formatted(client.getNamespace());

        // @formatter:off
        final var resource = new PersistentVolumeBuilder()
                .withNewMetadata()
                    .withName("registry-data")
                    .withNamespace(client.getNamespace())
                    .addToLabels("app", "registry-data")
                .endMetadata()
                .withNewSpec()
                    .withStorageClassName("local-storage")
                    .withCapacity(Map.of("storage", new Quantity("10Gi")))
                    .withAccessModes("ReadWriteOnce")
                    .withPersistentVolumeReclaimPolicy("Retain")
                    .withNewHostPath()
                        .withPath(path)
                    .endHostPath()
                    .withNewClaimRef()
                        .withName("registry-data")
                        .withNamespace(client.getNamespace())
                    .endClaimRef()
                .endSpec()
            .build();
        // @formatter:on
        final var volume = client.persistentVolumes().resource(resource);
        if (volume.get() == null) {
            volume.serverSideApply();
        }
    }

    private static void createPersistentVolumeClaim(KubernetesClient client) {
        // @formatter:off
        final var resource = new PersistentVolumeClaimBuilder()
                .withNewMetadata()
                    .withName("registry-data")
                    .withNamespace(client.getNamespace())
                .endMetadata()
                .withNewSpec()
                    .withNewSelector()
                        .addToMatchLabels("app", "registry-data")
                    .endSelector()
                    .withStorageClassName("local-storage")
                    .withAccessModes("ReadWriteOnce")
                    .withVolumeName("registry-data")
                    .withResources(new VolumeResourceRequirementsBuilder()
                        .addToRequests("storage", new Quantity("10Gi"))
                        .build())
                .endSpec()
            .build();
        // @formatter:on
        final var volume = client.persistentVolumeClaims().resource(resource);
        if (volume.get() == null) {
            volume.serverSideApply();
        }
    }

    private static void createDeployment(KubernetesClient client) {
        // @formatter:off
        final var resource = new DeploymentBuilder()
            .withNewMetadata()
                .withName("registry")
                .withNamespace(client.getNamespace())
            .endMetadata()
            .withNewSpec()
                .withReplicas(1)
                .withNewSelector()
                    .addToMatchLabels("app", "registry")
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", "registry")
                    .endMetadata()
                    .withNewSpec()
                        .withServiceAccountName("nextcluster")
                        .addNewContainer()
                            .withName("registry")
                            .withImage("registry:2.8.3")
                            .addNewPort()
                                .withContainerPort(5000)
                            .endPort()
                        .addNewVolumeMount()
                            .withName("registry-data")
                            .withMountPath("/var/lib/registry")
                        .endVolumeMount()
                        .endContainer()
                        .addNewVolume()
                            .withName("registry-data")
                            .withNewPersistentVolumeClaim()
                                .withClaimName("registry-data")
                            .endPersistentVolumeClaim()
                        .endVolume()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();
        // @formatter:on
        final var deployment = client.apps().deployments().resource(resource);
        if (deployment.get() == null) {
            deployment.serverSideApply();
        }
    }

    private static void createService(KubernetesClient client) {
        // @formatter:off
        final var resource = new ServiceBuilder()
            .withNewMetadata()
                .withName("registry")
                .withNamespace(client.getNamespace())
            .endMetadata()
            .withNewSpec()
                .withClusterIP("10.99.214.62")
                .addNewPort()
                    .withPort(5000)
                    .withTargetPort(new IntOrString(5000))
                    .withProtocol("TCP")
                .endPort()
                .withSelector(Map.of("app", "registry"))
            .endSpec()
            .build();
        // @formatter:on
        final var service = client.services().resource(resource);
        if (service.get() == null) {
            service.serverSideApply();
        }
    }
}
