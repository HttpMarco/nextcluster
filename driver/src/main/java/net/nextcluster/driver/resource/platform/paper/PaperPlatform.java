package net.nextcluster.driver.resource.platform.paper;

import net.nextcluster.driver.resource.platform.PaperMcPlatform;
import net.nextcluster.driver.resource.platform.VersionalPlatform;

public class PaperPlatform extends PaperMcPlatform implements VersionalPlatform {

    public PaperPlatform() {
        super("paper");
    }

    @Override
    public String getDownloadUrl(String version) {
        var lastBuildNumber = getLatestBuildnumber(version);
        return DOWNLOAD_URL.formatted(id(), version, lastBuildNumber, id(), version, lastBuildNumber);
    }
}
