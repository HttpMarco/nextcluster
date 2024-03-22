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

package net.nextcluster.assembler;

import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.osgan.utils.types.MessageUtils;
import lombok.SneakyThrows;
import net.nextcluster.assembler.image.ImageMeta;
import net.nextcluster.assembler.tasks.CommandLineTask;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.group.ClusterGroup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static net.nextcluster.driver.NextCluster.LOGGER;

public class FileWatcherThread extends Thread {

    private static final String DOCKER_IMAGE_FORMAT = "%s/%s:%s";
    private static final long THREAD_SLEEP_TICKS = TimeUnit.SECONDS.toMillis(5);
    private final Map<String, Long> lastChecked = new ConcurrentHashMap<>();
    private final Set<Path> buildQueue = new LinkedHashSet<>();

    public FileWatcherThread() {
        super("FileWatcherThread");
    }

    @SuppressWarnings({"BusyWait"})
    @SneakyThrows
    @Override
    public void run() {
        final var images = Path.of("/images");

        LOGGER.info("Watching: {}", images.toAbsolutePath());

        while (currentThread().isAlive()) {
            this.findChangedImages(images);

            if (!this.buildQueue.isEmpty()) {
                var path = this.buildQueue.stream().findFirst().orElseThrow();

                LOGGER.info("Rebuilding image: {}", path.toFile().getName());

                this.buildQueue.remove(path);

                var meta = path.resolve("meta.json");
                var dockerfile = path.resolve("Dockerfile");

                if (Files.notExists(dockerfile)) {
                    LOGGER.error("No Dockerfile found ({})", dockerfile);
                    return;
                }

                try {
                    final var metadata = JsonUtils.fromJson(Files.readString(meta), ImageMeta.class);
                    final var image = DOCKER_IMAGE_FORMAT.formatted(metadata.getUrl(), metadata.getName(), metadata.getTag());

                    CommandLineTask.run("docker build -t " + image + " " + path.toAbsolutePath());

                    if (metadata.getAuthorization() != null) {
                        CommandLineTask.run(
                                "docker", "login", metadata.getUrl(),
                                "-u", metadata.getAuthorization().getUsername(),
                                "-p", metadata.getAuthorization().getPassword()
                        );
                    }

                    CommandLineTask.run("docker push " + image);
                    LOGGER.info("Image {} built and pushed successfully", image);

                    if (metadata.isRestartAfterBuild()) {
                        LOGGER.info("Restart all pods with image {}.", image);
                        NextCluster.instance().groupProvider().groups().stream().filter(it -> it.image().equalsIgnoreCase(image)).forEach(ClusterGroup::shutdown);
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }

                LOGGER.info("Remaining build queue: {}", (this.buildQueue.isEmpty() ? "empty" : String.join(", ", this.buildQueue.stream().map(path1 -> path1.toFile().getName()).toList())));
            }

            Thread.sleep(THREAD_SLEEP_TICKS);
        }
    }

    private Path scanParent(Path start) {
        if (Files.exists(start.resolve("meta.json"))) {
            return start;
        }
        final var parent = start.getParent();
        if (parent == null) {
            return null;
        }
        return scanParent(parent);
    }

    private void findChangedImages(Path path) {
        var children = path.toFile().listFiles();

        if (children != null) {
            for (File child : children) {
                var lastCheck = this.lastChecked.computeIfAbsent(child.getAbsolutePath(), s -> (System.currentTimeMillis() - THREAD_SLEEP_TICKS));

                this.lastChecked.put(child.getAbsolutePath(), System.currentTimeMillis());

                if (this.isChanged(child.toPath(), lastCheck)) {
                    this.buildQueue.add(child.toPath());

                    LOGGER.info("Found changes in file {}", child.getName());
                }
            }
        }
    }

    private boolean isChanged(Path path, long lastCheck) {
        try (Stream<Path> paths = Files.walk(path)) {
            return paths.filter(p -> !Files.isDirectory(p))
                    .anyMatch(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis() > lastCheck;
                        } catch (IOException e) {
                            return false; // Suppress IOException
                        }
                    });
        } catch (IOException e) {
            return false; // Suppress IOException
        }
    }
}
