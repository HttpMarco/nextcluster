package net.nextcluster.module.proxy.config.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class TablistModel {

    private String header;
    private String footer;
    private long next = 3 * 20L;

}
