package net.nextcluster.driver.resource.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class Platform {

    private String id;

    public String type() {
        return this instanceof ProxyPlatform ? "PROXY" : "SERVER";
    }

}
