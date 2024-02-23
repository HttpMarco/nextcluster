package net.nextcluster.driver.resource.platform;

import net.nextcluster.driver.resource.platform.impl.BungeeCordPlatform;
import net.nextcluster.driver.resource.platform.impl.CustomPlatform;
import net.nextcluster.driver.resource.platform.impl.MinestomPlatform;
import net.nextcluster.driver.resource.platform.paper.PaperPlatform;
import net.nextcluster.driver.resource.platform.paper.VelocityPlatform;
import net.nextcluster.driver.resource.platform.paper.WaterfallPlatform;

import java.util.ArrayList;
import java.util.List;

public class PlatformService {

    public static final List<Platform> PLATFORMS = new ArrayList<>();

    static {
        PLATFORMS.add(new CustomPlatform());
        PLATFORMS.add(new PaperPlatform());
        PLATFORMS.add(new CustomPlatform());
        PLATFORMS.add(new WaterfallPlatform());
        PLATFORMS.add(new VelocityPlatform());
        PLATFORMS.add(new MinestomPlatform());
        PLATFORMS.add(new BungeeCordPlatform());
    }

    public static Platform detect() {
        var platformVersion = System.getenv("PLATFORM");
        return PLATFORMS.
                stream()
                .filter(it -> it.id().toUpperCase().equalsIgnoreCase(platformVersion) ||
                        (it instanceof VersionalPlatform && platformVersion.startsWith(it.id() + "_")))
                .findFirst()
                .orElse(null);

    }
}
