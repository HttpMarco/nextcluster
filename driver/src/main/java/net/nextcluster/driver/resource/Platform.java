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

package net.nextcluster.driver.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum Platform {

    VELOCITY("Velocity", true, "https://api.papermc.io/v2/projects/velocity/versions/3.3.0-SNAPSHOT/builds/350/downloads/velocity-3.3.0-SNAPSHOT-350.jar"),
    WATERFALL("Waterfall", true, "https://papermc.io/api/v2/projects/waterfall/versions/1.20/builds/562/downloads/waterfall-1.20-562.jar"),
    BUNGEECORD("BungeeCord", true, "https://ci.md-5.net/job/BungeeCord/lastBuild/artifact/bootstrap/target/BungeeCord.jar"),

    PAPER("Paper", false, "https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/409/downloads/paper-1.20.4-409.jar", true),
    MINESTOM(
        "Minestom",
        false,
        "https://nexus.bytemc.de/repository/maven-public/net/bytemc/server/1.6.1/server-1.6.1-all.jar",
        false,
        () -> {
            final List<String> args = new ArrayList<>();
            args.add("--disableMojangAuth");

            final var velocitySecret = System.getenv("VELOCITY_SECRET");
            if (velocitySecret != null) {
                args.add("--velocity");
                args.add(velocitySecret);
            }
            var fin = args.toArray(new String[0]);
            LoggerFactory.getLogger(Platform.class).info("Minestom args: " + Arrays.toString(fin));
            return fin;
        }
    ),

    CUSTOM("Custom");

    Platform(String id, boolean proxy, String url) {
        this(id, proxy, url, false, null);
    }

    Platform(String id, boolean proxy, String url, boolean eula) {
        this(id, proxy, url, eula, null);
    }

    Platform(String id, boolean proxy, String url, Supplier<String[]> args) {
        this(id, proxy, url, false, args);
    }

    private final String id;
    private boolean proxy;
    private String url;
    private boolean eula;
    private Supplier<String[]> args;

    public static Platform detect() {
        try {
            return valueOf(System.getenv("PLATFORM").toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public String[] args() {
        return this.args == null ? new String[0] : this.args.get();
    }

    public String type() {
        return this.proxy ? "PROXY" : "SERVER";
    }
}
