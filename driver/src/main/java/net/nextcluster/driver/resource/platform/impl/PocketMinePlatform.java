package net.nextcluster.driver.resource.platform.impl;

import net.nextcluster.driver.resource.platform.DownloadablePlatform;
import net.nextcluster.driver.resource.platform.PlatformType;

public class PocketMinePlatform extends DownloadablePlatform {
    public PocketMinePlatform() {
        super(PlatformType.SERVER,"POCKETMINE", "https://github.com/pmmp/PocketMine-MP/releases/download/5.12.0/PocketMine-MP.phar");
    }
}
