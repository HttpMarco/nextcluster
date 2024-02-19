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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.Platform;
import net.nextcluster.prevm.classloader.AccessibleClassLoader;
import net.nextcluster.prevm.networking.NettyClientTransmitter;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Accessors(fluent = true)
@Getter
public class PreVM extends NextCluster {

    private final String[] args;
    private final AccessibleClassLoader classLoader = new AccessibleClassLoader();
    @Setter(AccessLevel.PACKAGE)
    private Platform platform;

    protected PreVM(String[] args) {
        super(new NettyClientTransmitter());
        this.args = args;
    }

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile("platform.jar"));
        } catch (IOException ignore) {
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        final PreVM preVM = new PreVM(args);

        final String env = System.getenv("PLATFORM");
        if (env == null) {
            throw new IllegalStateException("No PLATFORM environment variable found!");
        }
        try {
            preVM.platform(Platform.valueOf(env));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("No platform found for " + env +
                    "(" + Arrays.stream(Platform.values()).map(Enum::name).collect(Collectors.joining(", ")) + ")"
            );
        }

        final var platform = Path.of("platform.jar");
        if (Files.notExists(platform)) {
            LOGGER.warn("No platform.jar found, downloading platform...");
            preVM.downloadPlatform(platform);
        }

        if (System.getenv().containsKey("STATIC") && Boolean.parseBoolean(System.getenv("STATIC"))) {
            final Path staticFolder = Path.of("./static");
            if (Files.notExists(staticFolder)) {
                Files.copy(Path.of("/data"), staticFolder);
            }
            preVM.startPlatform(new File("./static/platform.jar"));
            return;
        }
        preVM.startPlatform(platform.toFile());
    }

    @SneakyThrows
    private void startPlatform(File file) {
        final var jar = new JarFile(file);

        classLoader.addURL(file.toURI().toURL());

        if (this.platform.eula()) {
            File eula = new File("eula.txt");

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
            final var mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            jar.close();

            LOGGER.info("Invoke Main-Class ({})", mainClass);
            final var main = Class.forName(mainClass, true, classLoader).getMethod("main", String[].class);
            main.invoke(null, (Object) platform.args());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void downloadPlatform(Path path) {
        try {
            final URL url = URI.create(platform.url()).toURL();
            Files.copy(url.openConnection().getInputStream(), path);
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
