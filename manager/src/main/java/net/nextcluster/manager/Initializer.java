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

package net.nextcluster.manager;

import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.nextcluster.manager.requirements.Assembler;
import net.nextcluster.manager.requirements.Registry;

class Initializer {

    private static final String IDENTIFIER = "nextcluster";

    static void initialize(KubernetesClient client) {
        createServiceAccount(client);
        createRole(client);
        createRoleBinding(client);

        if (System.getenv("NO_CUSTOM_REGISTRY") == null) {
            Registry.initialize(client);
        }
        Assembler.initialize(client);
    }

    private static void createServiceAccount(KubernetesClient client) {
        // @formatter:off
        final var serviceAccount = new ServiceAccountBuilder()
            .withNewMetadata()
                .withName(IDENTIFIER)
                .withNamespace(client.getNamespace())
            .endMetadata()
            .build();
        // @formatter:on
        client.serviceAccounts().resource(serviceAccount).serverSideApply();
    }

    private static void createRole(KubernetesClient client) {
        // @formatter:off
        final var role = new RoleBuilder()
            .withNewMetadata()
                .withName(IDENTIFIER)
                .withNamespace(client.getNamespace())
            .endMetadata()
            .addNewRule()
                .withApiGroups("", "apps", "nextcluster.net")
                .withResources(
                    "pods", "pods/log", "pods/exec", "deployments", "services", "statefulsets",
                    "events", "configmaps", "groups")
                .withVerbs("get", "list", "watch", "create", "update", "delete", "patch")
            .endRule()
            .build();
        // @formatter:on
        client.rbac().roles().resource(role).serverSideApply();
    }

    private static void createRoleBinding(KubernetesClient client) {
        // @formatter:off
        final var roleBinding = new RoleBindingBuilder()
            .withNewMetadata()
                .withName(IDENTIFIER)
                .withNamespace(client.getNamespace())
            .endMetadata()
            .withSubjects(new SubjectBuilder()
                .withKind("ServiceAccount")
                .withName(IDENTIFIER)
                .withNamespace(client.getNamespace())
                .build())
            .withNewRoleRef()
                .withApiGroup("rbac.authorization.k8s.io")
                .withKind("Role")
                .withName(IDENTIFIER)
            .endRoleRef()
            .build();
        // @formatter:on
        client.rbac().roleBindings().resource(roleBinding).serverSideApply();
    }


}
