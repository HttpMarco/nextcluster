subprojects {
    repositories {
        // Velocity
        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        // WaterdogPE
        maven {
            name = "waterdogpeRepoSnapshots"
            url = uri("https://repo.waterdog.dev/snapshots")
        }
    }

    dependencies {
        compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
        annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
    }
}