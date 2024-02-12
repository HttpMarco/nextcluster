package net.nextcluster.driver.event.service;

import net.nextcluster.driver.resource.service.ClusterService;

public class ClusterServiceShutdownEvent extends ClusterServiceEvent {

    public ClusterServiceShutdownEvent(ClusterService service) {
        super(service);
    }
}
