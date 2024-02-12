package net.nextcluster.driver.event.service;

import net.nextcluster.driver.resource.service.ClusterService;

public class ClusterServiceStartEvent extends ClusterServiceEvent {

    public ClusterServiceStartEvent(ClusterService service) {
        super(service);
    }
}
