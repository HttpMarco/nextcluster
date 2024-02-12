package net.nextcluster.driver.event.service;

import net.nextcluster.driver.resource.service.ClusterService;

public class ClusterServiceUpdateEvent extends ClusterServiceEvent {

    public ClusterServiceUpdateEvent(ClusterService service) {
        super(service);
    }
}
