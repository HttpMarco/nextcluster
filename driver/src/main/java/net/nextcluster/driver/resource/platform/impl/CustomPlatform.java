package net.nextcluster.driver.resource.platform.impl;

import net.nextcluster.driver.resource.platform.Platform;
import net.nextcluster.driver.resource.platform.PlatformType;

public final class CustomPlatform extends Platform {

    public CustomPlatform() {
        super(PlatformType.CUSTOM, "custom");
    }
}
