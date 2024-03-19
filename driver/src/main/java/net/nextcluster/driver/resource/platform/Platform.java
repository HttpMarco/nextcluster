package net.nextcluster.driver.resource.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.resource.platform.impl.CustomPlatform;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class Platform {

    private String id;

    public String type() {
        if (this instanceof ProxyPlatform) {
            return "PROXY";
        } else if (this instanceof CustomPlatform) {
            return "CUSTOM";
        } else {
            return "SERVER";
        }
    }

}
