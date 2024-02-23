package net.nextcluster.prevm.exception;

import net.nextcluster.driver.resource.platform.Platform;
import net.nextcluster.driver.resource.platform.PlatformService;

import java.util.stream.Collectors;

public class NoPlatformFoundException extends RuntimeException {

    public NoPlatformFoundException() {
        super("No platform found for " + System.getenv("PLATFORM") +
                "(" +
                PlatformService.PLATFORMS.stream().map(Platform::id).collect(Collectors.joining(", ")) +
                ")");
    }
}
