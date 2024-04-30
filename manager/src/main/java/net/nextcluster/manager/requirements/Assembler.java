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

public class Assembler {

    public static void initialize(KubernetesClient client) {
        createPersistentVolume(client);
        createPersistentVolumeClaim(client);
        createDeployment(client);
    }

    private static void createPersistentVolume(KubernetesClient client) {
        final var path = "/srv/nextcluster/%s/images".formatted(client.getNamespace());

        // @formatter:off
        final var resource = new PersistentVolumeBuilder()
            .withNewMetadata()
                .withName("assembler-data-" + client.getNamespace())
                .addToLabels("app", "assembler-data-" + client.getNamespace())
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
                    .withName("assembler-data-" + client.getNamespace())
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
                .withName("assembler-data-" + client.getNamespace())
                .withNamespace(client.getNamespace())
            .endMetadata()
            .withNewSpec()
                .withStorageClassName("local-storage")
                .withNewSelector()
                    .withMatchLabels(Map.of("app", "assembler-data-" + client.getNamespace()))
                .endSelector()
                .withAccessModes("ReadWriteOnce")
                .withResources(new VolumeResourceRequirementsBuilder()
                    .addToRequests("storage", new Quantity("20Gi"))
                    .build())
                .withVolumeName("assembler-data-" + client.getNamespace())
            .endSpec()
            .build();
        // @formatter:on
        final var claim = client.persistentVolumeClaims().resource(resource);
        if (claim.get() == null) {
            claim.serverSideApply();
        }
    }

    private static void createDeployment(KubernetesClient client) {
        // @formatter:off
        final var resource = new DeploymentBuilder()
            .withNewMetadata()
                .withName("assembler")
                .withNamespace(client.getNamespace())
            .endMetadata()
            .withNewSpec()
                .withReplicas(1)
                .withNewSelector()
                    .addToMatchLabels("app", "assembler")
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", "assembler")
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName("assembler")
                            .withImage("registry.nextcluster.net/assembler:latest")
                            .addNewVolumeMount()
                                .withName("docker-socket")
                                .withMountPath("/var/run/docker.sock")
                            .endVolumeMount()
                            .addNewVolumeMount()
                                .withName("assembler-data-" + client.getNamespace())
                                .withMountPath("/images")
                            .endVolumeMount()
                        .endContainer()
                        .addNewVolume()
                            .withName("docker-socket")
                            .withNewHostPath()
                                .withPath("/var/run/docker.sock")
                            .endHostPath()
                        .endVolume()
                        .addNewVolume()
                            .withName("assembler-data-" + client.getNamespace())
                            .withNewPersistentVolumeClaim()
                                .withClaimName("assembler-data-" + client.getNamespace())
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

}
