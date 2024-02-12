package net.nextcluster.driver.event.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.nextcluster.driver.event.ClusterEvent;
import net.nextcluster.driver.resource.service.ClusterService;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ClusterServiceEvent implements ClusterEvent {

    private final ClusterService service;



}
