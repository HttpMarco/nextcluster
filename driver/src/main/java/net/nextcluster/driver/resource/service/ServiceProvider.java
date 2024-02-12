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

import com.google.common.collect.ImmutableList;
import net.nextcluster.driver.NextCluster;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public class ServiceProvider {

    @UnmodifiableView
    public ImmutableList<ClusterService> getServices() {
        return ImmutableList.copyOf(
            NextCluster.instance()
                .kubernetes()
                .pods()
                .list()
                .getItems()
                .stream()
                .map(SimpleClusterService::new)
                .toList()
        );
    }

    public CompletionStage<ImmutableList<ClusterService>> getServicesAsync() {
        return CompletableFuture.supplyAsync(this::getServices);
    }

    public Optional<ClusterService> getService(String name) {
        final var pod = NextCluster.instance().kubernetes().pods().withName(name).get();
        if (pod == null) {
            return Optional.empty();
        }
        return Optional.of(new SimpleClusterService(pod));
    }

    public CompletionStage<Optional<ClusterService>> getServiceAsync(String name) {
        return CompletableFuture.supplyAsync(() -> getService(name));
    }

}
