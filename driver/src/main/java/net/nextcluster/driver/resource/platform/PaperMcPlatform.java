package net.nextcluster.driver.resource.platform;

import dev.httpmarco.osgan.files.json.JsonObjectSerializer;
import net.nextcluster.driver.NextCluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class PaperMcPlatform extends DownloadablePlatform {

    private static final String LATEST_VERSION_URL = "https://papermc.io/api/v2/projects/%s";
    private static final String LATEST_BUILDNUMBER_URL = "https://papermc.io/api/v2/projects/%s/versions/%s";
    public static final String DOWNLOAD_URL = "https://api.papermc.io/v2/projects/%s/versions/%s/builds/%s/downloads/%s-%s-%s.jar";

    public PaperMcPlatform(String type, String id) {
        super(type, id, null);
    }

    @Override
    public String getDownloadURL() {
        var version = getLatestVersion();
        var lastBuildNumber = getLatestBuildnumber(version);
        return DOWNLOAD_URL.formatted(id(), version, lastBuildNumber, id(), version, lastBuildNumber);
    }


    public String getLatestBuildnumber(String version) {
        var response = new JsonObjectSerializer(getURLResponse(String.format(LATEST_BUILDNUMBER_URL, id(), version)));
        var builds = response.getJsonObject().getAsJsonArray("builds");
        return builds.get(builds.size() - 1).getAsString();
    }


    public String getLatestVersion() {
        var response = new JsonObjectSerializer(getURLResponse(String.format(LATEST_VERSION_URL, id())));
        var groups = response.getJsonObject().getAsJsonArray("versions");
        return groups.get(groups.size() - 1).getAsString();
    }

    public static String getURLResponse(String urlString) {
        var response = new StringBuilder();
        try {
            var url = new URI(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
            } else {
                NextCluster.LOGGER.error("Platform error: {}", responseCode);
            }
            connection.disconnect();
        } catch (IOException | URISyntaxException e) {
            NextCluster.LOGGER.error("Platform error: {}", e.getMessage());
        }
        return response.toString();
    }
}
