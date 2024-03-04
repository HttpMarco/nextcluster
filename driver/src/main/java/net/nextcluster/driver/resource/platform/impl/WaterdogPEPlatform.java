package net.nextcluster.driver.resource.platform.impl;

import net.nextcluster.driver.resource.platform.DownloadablePlatform;
import net.nextcluster.driver.resource.platform.ProxyPlatform;

public class WaterdogPEPlatform extends DownloadablePlatform implements ProxyPlatform {

    public WaterdogPEPlatform() {
        super("WATERDOGPE", "https://repo.waterdog.dev/snapshots/dev/waterdog/waterdogpe/waterdog/2.0.2-SNAPSHOT/waterdog-2.0.2-20240213.170000-3.jar");
    }
}
