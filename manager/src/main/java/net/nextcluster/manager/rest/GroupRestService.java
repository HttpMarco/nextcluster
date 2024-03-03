package net.nextcluster.manager.rest;

import net.nextcluster.driver.NextCluster;
import net.nextcluster.driver.resource.Platform;
import net.nextcluster.driver.resource.group.ClusterGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController("/group")
public final class GroupRestService {

    @GetMapping("/list")
    public List<ClusterGroup> findGroup() {
        return new ArrayList<>();
    }

    @GetMapping("/group")
    public ClusterGroup findGroup(@RequestParam String id) {
        return NextCluster.instance().groupProvider().group(id).orElse(null);
    }

    @DeleteMapping("/delete")
    public void deleteGroup(@RequestParam String id) {
        NextCluster.instance().groupProvider().delete(id);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createGroup(@RequestParam String id,
                                              @RequestParam(defaultValue = "1") int maxOnline,
                                              @RequestParam(defaultValue = "1") int minOnline,
                                              @RequestParam(defaultValue = "false") boolean fallback,
                                              @RequestParam(defaultValue = "512") int maxMemory,
                                              @RequestParam(defaultValue = "CUSTOM") String platform) {

        NextCluster.instance().groupProvider().create(id)
                .withMaxOnline(maxOnline)
                .withMinOnline(minOnline)
                .withFallback(fallback)
                .withMaxMemory(maxMemory)
                .withPlatform(Platform.valueOf(platform))
                .publish();

        return ResponseEntity.ok().build();
    }
}