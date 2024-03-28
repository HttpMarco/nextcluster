package net.nextcluster.driver.resource.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.resource.platform.impl.CustomPlatform;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class Platform {

    private PlatformType type;
    private String id;

}
