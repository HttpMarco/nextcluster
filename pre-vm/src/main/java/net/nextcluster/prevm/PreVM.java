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
import net.nextcluster.driver.resource.platform.DownloadablePlatform;
import net.nextcluster.driver.resource.platform.Platform;
import net.nextcluster.driver.resource.platform.PlatformArgs;
import net.nextcluster.driver.resource.platform.PlatformService;
import net.nextcluster.driver.resource.platform.paper.PaperPlatform;
import net.nextcluster.prevm.classloader.AccessibleClassLoader;
import net.nextcluster.prevm.exception.NoPlatformFoundException;
import net.nextcluster.prevm.networking.NettyClientTransmitter;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

@Accessors(fluent = true)
@Getter
public class PreVM extends NextCluster {

    private static final Path WORKING_DIR = Path.of("/data");

    private final String[] args;
    private final AccessibleClassLoader classLoader = new AccessibleClassLoader();
    @Setter(AccessLevel.PACKAGE)
    private Platform platform;

    private PreVM(String[] args) {
        super(new NettyClientTransmitter());
        this.args = args;
    }

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(WORKING_DIR.resolve("platform.jar").toFile()));
        } catch (Exception ignore) {
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
        preVM.startPlatform(platform.toFile());
    }

    @SneakyThrows
    private void startPlatform(File file) {
        final var jar = new JarFile(file);

        classLoader.addURL(file.toURI().toURL());

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
            final var mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            jar.close();

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
