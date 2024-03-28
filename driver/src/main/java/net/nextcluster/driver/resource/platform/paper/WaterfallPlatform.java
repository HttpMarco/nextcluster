package net.nextcluster.driver.resource.platform.paper;

import net.nextcluster.driver.resource.platform.PaperMcPlatform;
import net.nextcluster.driver.resource.platform.PlatformType;
import net.nextcluster.driver.resource.platform.ProxyPlatform;

public class WaterfallPlatform extends PaperMcPlatform implements ProxyPlatform {

    public WaterfallPlatform() {
        super(PlatformType.PROXY, "waterfall");
    }


    public static void main(String[] args) {
        var platform = new VelocityPlatform();
    }
}
