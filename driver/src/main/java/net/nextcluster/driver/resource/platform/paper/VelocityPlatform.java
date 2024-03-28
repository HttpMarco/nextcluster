package net.nextcluster.driver.resource.platform.paper;

import net.nextcluster.driver.resource.platform.PaperMcPlatform;
import net.nextcluster.driver.resource.platform.PlatformType;
import net.nextcluster.driver.resource.platform.ProxyPlatform;

public class VelocityPlatform extends PaperMcPlatform implements ProxyPlatform {

    public VelocityPlatform() {
        super(PlatformType.PROXY, "velocity");
    }
}
