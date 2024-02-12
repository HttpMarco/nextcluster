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

import io.fabric8.kubernetes.api.model.PersistentVolumeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Map;

public class Registry {

    public static void initialize(KubernetesClient client) {
        createPersistentVolume(client);
    }

    private static void createPersistentVolume(KubernetesClient client) {
        // @formatter:off
        final var resource = new PersistentVolumeBuilder()
                .withNewMetadata()
                    .withName("nextcluster")
                .endMetadata()
                .withNewSpec()
                    .withStorageClassName("manual")
                    .withCapacity(Map.of("storage", new Quantity("10Gi")))
                    .withAccessModes("ReadWriteOnce")
                    .withPersistentVolumeReclaimPolicy("Retain")
                    .withNewHostPath()
                        .withPath("/srv/nextcluster/registry")
                    .endHostPath()
                .endSpec()
            .build();
        // @formatter:on
        final var volume = client.persistentVolumes().resource(resource);
        if (volume.get() == null) {
            volume.serverSideApply();
        }
    }


}
