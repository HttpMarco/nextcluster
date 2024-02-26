package net.nextcluster.manager.rest;

import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.service.ClusterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/service")
public final class ServiceRestService {

    @GetMapping("/log")
    public ResponseEntity<String[]> readLog(@RequestParam String id) {
        var service = NextCluster.instance().serviceProvider().service(id);
        return service.map(it -> ResponseEntity.ofNullable(it.asResource().getLog().split("\n", -1)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<List<ClusterService>> readServices() {
        return ResponseEntity.ofNullable(NextCluster.instance().serviceProvider().services());
    }
}
