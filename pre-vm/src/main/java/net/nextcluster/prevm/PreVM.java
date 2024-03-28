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

package net.nextcluster.prevm;

import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.osgan.networking.client.NettyClient;
import dev.httpmarco.osgan.networking.server.NettyServer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.event.ClusterEvent;
import net.nextcluster.driver.event.ClusterEventCallPacket;
import net.nextcluster.driver.resource.platform.DownloadablePlatform;
import net.nextcluster.driver.resource.platform.Platform;
import net.nextcluster.driver.resource.platform.PlatformArgs;
import net.nextcluster.driver.resource.platform.PlatformService;
import net.nextcluster.driver.resource.platform.paper.PaperPlatform;
import net.nextcluster.driver.transmitter.NetworkTransmitter;
import net.nextcluster.prevm.classloader.AccessibleClassLoader;
import net.nextcluster.prevm.exception.NoPlatformFoundException;
import net.nextcluster.prevm.networking.NettyClientTransmitter;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

@Accessors(fluent = true)
@Getter
public class PreVM extends NextCluster {

    private static final Path WORKING_DIR = Path.of("/data");
    private static Instrumentation instrumentation;

    private final String[] args;
    private final NettyClient nettyClient;
    private AccessibleClassLoader classLoader;
    @Setter(AccessLevel.PACKAGE)
    private Platform platform;

    private PreVM(String[] args) {
        super(new NettyClientTransmitter());

        this.args = args;

        var managerIp = NextCluster.instance()
                .kubernetes()
                .pods()
                .withLabel("app", "manager")
                .resources()
                .findFirst()
                .orElse(null);

        if (managerIp != null) {
            NextCluster.LOGGER.info("Trying to conect to manager on '" + managerIp.get().getStatus().getPodIP() + ":" + NetworkTransmitter.NETTY_PORT + "'");
        } else {
            //TODO maybe dont throw exception?
            throw new RuntimeException("Could not find manager pod! Exiting...");
        }

        var nettyBuilder = NettyClient.builder()
                .withHostname(managerIp.get().getStatus().getPodIP())
                .withPort(NetworkTransmitter.NETTY_PORT)
                .withReconnect(TimeUnit.SECONDS, 5);

        if (System.getenv("NETTY_CLIENT_ID") != null) {
            nettyBuilder.withId(System.getenv("NETTY_CLIENT_ID"));
        }

        this.nettyClient = nettyBuilder.build();

        transmitter().listen(ClusterEventCallPacket.class, (channel, packet) -> {
            try {
                NextCluster.instance().eventRegistry().callLocal(JsonUtils.fromJson(packet.json(), (Class<? extends ClusterEvent>) Class.forName(packet.eventClass())));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void premain(String args, Instrumentation instrumentation) {
        PreVM.instrumentation = instrumentation;
    }

    @SneakyThrows
    public static void main(String[] args) {
        final PreVM preVM = new PreVM(args);
        final String env = System.getenv("PLATFORM");
        if (env == null) {
            throw new IllegalStateException("No PLATFORM environment variable found!");
        }
        try {
            preVM.platform(PlatformService.detect());
        } catch (IllegalArgumentException e) {
            throw new NoPlatformFoundException();
        }

        final var platform = WORKING_DIR.resolve("platform.jar");
        if (Files.notExists(platform)) {
            LOGGER.warn("No platform.jar found, downloading platform...");
            if (platform instanceof DownloadablePlatform downloadablePlatform) {
                downloadablePlatform.download(WORKING_DIR);
            }
        }

        if (!env.equalsIgnoreCase("PAPER")) {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(platform.toFile()));
        }
        preVM.startPlatform(platform.toFile());
    }

    @SneakyThrows
    private void startPlatform(File file) {
        this.classLoader = new AccessibleClassLoader(new URL[]{file.toURI().toURL()});
        try (final var jar = new JarFile(file)) {
            final var mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            if (this.platform instanceof PaperPlatform) {
                final var eula = WORKING_DIR.resolve("eula.txt").toFile();
                try {
                    if (!eula.exists() && eula.createNewFile()) {
                        LOGGER.info("No eula.txt found, accepting EULA...");
                        Files.writeString(eula.toPath(), "eula=true");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                LOGGER.info("Invoke Main-Class ({})", mainClass);
                final var main = Class.forName(mainClass, true, classLoader).getMethod("main", String[].class);

                if (platform instanceof PlatformArgs platformArgs) {
                    main.invoke(null, (Object) platformArgs.args());
                } else {
                    main.invoke(null, (Object) new String[0]);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
