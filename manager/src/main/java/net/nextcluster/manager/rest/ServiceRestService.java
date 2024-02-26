package net.nextcluster.manager.rest;

import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.service.ClusterService;
import net.nextcluster.driver.resource.service.ServiceInformation;
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

    @GetMapping
    public ResponseEntity<List<ServiceInformation>> readServices() {
        return ResponseEntity.ofNullable(NextCluster.instance().serviceProvider().services().stream().map(ClusterService::information).toList());
    }

    @GetMapping
    public ResponseEntity<ServiceInformation> readService(@RequestParam String id) {
        return ResponseEntity.ofNullable(NextCluster.instance().serviceProvider().service(id).map(ClusterService::information).orElseGet(null));
    }

    @PostMapping
    public ResponseEntity<?> shutdownService(@RequestParam String id) {
        var service = NextCluster.instance().serviceProvider().service(id);
        if (service.isPresent()) {
            service.get().shutdown();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.noContent().build();
    }


    @PostMapping
    public ResponseEntity<?> executeService(@RequestParam String id, @RequestParam String command) {
        var service = NextCluster.instance().serviceProvider().service(id);
        if (service.isPresent()) {
            service.get().execute(command);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.noContent().build();
    }
}
