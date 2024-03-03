package net.nextcluster.driver.resource.platform;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DownloadablePlatform extends Platform {

    private final String downloadUrl;

    public DownloadablePlatform(String id, String downloadUrl) {
        super(id);
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadURL() {
        return downloadUrl;
    }

    public void download(Path path) {
        if (this instanceof VersionalPlatform versionalPlatform) {
            var subVersion = System.getenv("PLATFORM").substring(id().length() + 1);
            this.downloadFile(versionalPlatform.getDownloadUrl(subVersion), path);
        } else {
            this.downloadFile(getDownloadURL(), path);
        }
    }

    private void downloadFile(String link, Path path) {
        try {
            final URL url = URI.create(link).toURL();
            Files.copy(url.openConnection().getInputStream(), path);
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace(System.err);
        }
    }

}
