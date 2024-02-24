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

import lombok.SneakyThrows;
import net.nextcluster.driver.NextCluster;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GroupProvider {

    public NextGroup.Builder create(String name) {
        return new NextGroup.Builder(name);
    }

    @SneakyThrows
    @UnmodifiableView
    public List<ClusterGroup> groups() {
        return NextCluster.instance()
            .kubernetes()
            .resources(NextGroup.class)
            .list()
            .getItems()
            .stream()
            .map(group -> (ClusterGroup) group)
            .toList();
    }

    @SneakyThrows
    @UnmodifiableView
    public CompletionStage<List<ClusterGroup>> groupsAsync() {
        return CompletableFuture.supplyAsync(this::groups);
    }

    public Optional<ClusterGroup> group(String name) {
        return Optional.ofNullable(NextCluster.instance().kubernetes().resources(NextGroup.class).withName(name).get());
    }

    public CompletionStage<Optional<ClusterGroup>> groupAsync(String name) {
        return CompletableFuture.supplyAsync(() -> group(name));
    }

    public void delete(String name) {
        NextCluster.instance().kubernetes().resources(NextGroup.class).withName(name).delete();
    }
}
