package net.nextcluster.driver.resource.platform.impl;

import net.nextcluster.driver.resource.platform.DownloadablePlatform;
import net.nextcluster.driver.resource.platform.ProxyPlatform;

public class BungeeCordPlatform extends DownloadablePlatform implements ProxyPlatform {

    public BungeeCordPlatform() {
        super("BUNGEECORD", "https://ci.md-5.net/job/BungeeCord/lastBuild/artifact/bootstrap/target/BungeeCord.jar");
    }

}
