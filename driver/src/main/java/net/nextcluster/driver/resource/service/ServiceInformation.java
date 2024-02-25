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

import dev.httpmarco.osgan.utils.data.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.nextcluster.driver.resource.Platform;
import net.nextcluster.driver.utils.SystemUtils;

import java.util.Collection;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
public final class ServiceInformation {

    private final String name = System.getenv("HOSTNAME");
    private final int onlinePlayers;
    private final int maxPlayers;
    private final String motd;
    private final Platform platform = Platform.detect();
    private final Collection<Pair<UUID, String>> players;

    // Memory
    private final double cpu = SystemUtils.cpuUsage();
    private final long memoryUsage = SystemUtils.memoryUsage();
    private final long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);

}
