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

package net.nextcluster.driver;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.nextcluster.driver.event.EventRegistry;
import net.nextcluster.driver.exceptions.NoNamespaceFoundException;
import net.nextcluster.driver.messaging.MessageService;
import net.nextcluster.driver.transmitter.NetworkTransmitter;
import net.nextcluster.driver.resource.config.ConfigProvider;
import net.nextcluster.driver.resource.group.GroupProvider;
import net.nextcluster.driver.resource.player.PlayerProvider;
import net.nextcluster.driver.resource.service.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Accessors(fluent = true)
public abstract class NextCluster {

    public static final Logger LOGGER = LoggerFactory.getLogger(NextCluster.class);
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    @Getter
    private static NextCluster instance;

    private final KubernetesClient kubernetes;
    private final ConfigProvider configProvider;
    private final GroupProvider groupProvider;
    private final ServiceProvider serviceProvider;
    private final NetworkTransmitter transmitter;
    private final EventRegistry eventRegistry;
    private final MessageService messageService;

    @Setter
    private @Nullable PlayerProvider playerProvider;

    protected NextCluster(NetworkTransmitter transmitter) {
        instance = this;

        if (!System.getProperties().containsKey("disable.banner")) {
            logBanner();
        }

        final String namespace = System.getProperties().computeIfAbsent("namespace", key -> namespace()).toString();

        LOGGER.info("Using namespace: {}", namespace);
        System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, namespace);

        LOGGER.info("Initializing Kubernetes client...");

        this.kubernetes = new KubernetesClientBuilder().build();

        this.transmitter = transmitter;
        this.configProvider = new ConfigProvider();
        this.groupProvider = new GroupProvider();
        this.serviceProvider = new ServiceProvider();
        this.eventRegistry = new EventRegistry();
        this.messageService = new MessageService();
    }

    private void logBanner() {
        LOGGER.info(" ");
        LOGGER.info(" _   _           _    _____ _           _            ");
        LOGGER.info("| \\ | |         | |  / ____| |         | |           ");
        LOGGER.info("|  \\| | _____  _| |_| |    | |_   _ ___| |_ ___ _ __ ");
        LOGGER.info("| . ` |/ _ \\ \\/ / __| |    | | | | / __| __/ _ \\ '__|");
        LOGGER.info("| |\\  |  __/>  <| |_| |____| | |_| \\__ \\ ||  __/ |   ");
        LOGGER.info("|_| \\_|\\___/_/\\_\\\\__|\\_____|_|\\__,_|___/\\__\\___|_|");
        LOGGER.info(" ");
        LOGGER.info(" Version: " + getClass().getPackage().getImplementationVersion());
        LOGGER.info(" GitHub: https://github.com/nextCluster/nextCluster");
        LOGGER.info(" ");
    }

    @SneakyThrows
    private String namespace() {
        // Check if we are running in a Kubernetes container
        final Path file = Path.of("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        if (Files.exists(file)) {
            return Files.readString(file);
        }
        throw new NoNamespaceFoundException("Found no namespace to initialize");
    }
}
