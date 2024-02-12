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

package net.nextcluster.driver.resource.service;

import dev.httpmarco.osgon.configuration.gson.JsonUtils;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.exceptions.ServiceNotRespondException;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Getter
@Accessors(fluent = true)
public class SimpleClusterService implements ClusterService {

    private final String name;

    public SimpleClusterService(Pod pod) {
        this.name = pod.getMetadata().getName();
    }

    @SneakyThrows
    @Override
    public ServiceInformation information() {
        final var request = HttpRequest.newBuilder()
            .build();
        final var response = NextCluster.HTTP_CLIENT.send(
            request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() != 200) {
            throw new ServiceNotRespondException("Failed to retrieve service information from " + this.name);
        }
        return JsonUtils.fromJson(response.body(), ServiceInformation.class);
    }

    @Override
    public void shutdown() {
        NextCluster.instance().kubernetes().pods().withName(this.name).delete();
    }
}
