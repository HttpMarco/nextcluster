package net.nextcluster.driver.resource.platform.impl;

import net.nextcluster.driver.resource.platform.DownloadablePlatform;
import net.nextcluster.driver.resource.platform.Platform;
import net.nextcluster.driver.resource.platform.PlatformArgs;
import net.nextcluster.driver.resource.platform.PlatformType;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinestomPlatform extends DownloadablePlatform implements PlatformArgs {

    public MinestomPlatform() {
        //todo find newest version
        super(PlatformType.SERVER, "MINESTOM", "https://nexus.bytemc.de/repository/maven-public/net/minestom/bootstrap/1.0.8-SNAPSHOT/bootstrap-1.0.8-SNAPSHOT.jar");
    }


    @Override
    public String[] args() {
        final List<String> args = new ArrayList<>();
        args.add("--disableMojangAuth");

        final var velocitySecret = System.getenv("VELOCITY_SECRET");
        if (velocitySecret != null) {
            args.add("--velocity");
            args.add(velocitySecret);
        }
        var fin = args.toArray(new String[0]);
        LoggerFactory.getLogger(Platform.class).info("Minestom args: {}", Arrays.toString(fin));
        return fin;
    }
}