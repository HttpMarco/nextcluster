### nextCluster

- Groups
- Services 
- Players
- Packets
- Events
- MessageChannel


```java
NextCluster.instance().groupProvider().get("Lobby").ifPresent(clusterGroup -> {
    // name of the group
    clusterGroup.name();
    // image name (template)
    clusterGroup.image();
    // minimum online pods
    clusterGroup.minOnline();
    // maximum online pods
    clusterGroup.minOnline();
    // maintenance parameter
    clusterGroup.isMaintenance();
    // shutdown all current running pods
    clusterGroup.shutdown();
    // all environment variables
    var variables = clusterGroup.environment();
});
```
```java
 var groups = NextCluster.instance().groupProvider().list();
```
```java
NextCluster.instance().groupProvider().create("Proxy").publish();
```
```java
NextCluster.instance().groupProvider().delete("Proxy");
```