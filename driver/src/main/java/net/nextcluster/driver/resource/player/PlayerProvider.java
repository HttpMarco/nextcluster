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

package net.nextcluster.driver.resource.player;

import dev.httpmarco.osgan.utils.executers.ThreadAsyncExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerProvider {

    ThreadAsyncExecutor<List<ClusterPlayer>> playersAsync();

    default List<ClusterPlayer> players() {
        return this.playersAsync().sync(new ArrayList<>());
    }

    default Optional<ClusterPlayer> player(UUID uniqueId) {
        return this.playerAsync(uniqueId).sync(Optional.empty());
    }

    ThreadAsyncExecutor<Optional<ClusterPlayer>> playerAsync(UUID uniqueId);

    default Optional<ClusterPlayer> player(String name) {
        return this.playerAsync(name).sync(Optional.empty());
    }

    ThreadAsyncExecutor<Optional<ClusterPlayer>> playerAsync(String name);

    default boolean isOnline(String username) {
        return this.isOnlineAsync(username).sync(false);
    }

    ThreadAsyncExecutor<Boolean> isOnlineAsync(String username);

    default boolean isOnline(UUID uniqueId) {
        return this.isOnlineAsync(uniqueId).sync(false);
    }

    ThreadAsyncExecutor<Boolean> isOnlineAsync(UUID uniqueId);

    ClusterPlayer createPlayer(String name, UUID uniqueId, String currentProxyName, String currentServerName);

}
