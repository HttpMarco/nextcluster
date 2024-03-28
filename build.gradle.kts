/*
 * MIT License
 *
 * Copyright (c) 2024 nextCluster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

plugins {
    `java-library`
    `maven-publish`
}

subprojects {
    group = "net.nextcluster"
    version = "1.0.2-SNAPSHOT"

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        maven(url = "https://nexus.bytemc.de/repository/maven-public/")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://repo.waterdog.dev/snapshots")
        maven(url = "https://repo.opencollab.dev/maven-releases/")
        maven(url = "https://repo.opencollab.dev/maven-snapshots/")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    afterEvaluate {
        dependencies {
            compileOnly(libs.lombok)
            annotationProcessor(libs.lombok)

            testCompileOnly(libs.lombok)
            testAnnotationProcessor(libs.lombok)
            testImplementation(platform("org.junit:junit-bom:5.10.2"))
            testImplementation("org.junit.jupiter:junit-jupiter")
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes["Implementation-Version"] = version
        }
    }

    if (hasProperty("REPOSITORY_USERNAME") && project.name == "driver") {
        publishing {
            publications {
                create<MavenPublication>("mavenJava") {
                    this.groupId = group.toString()
                    this.artifactId = artifactId
                    this.version = version.toString()

                    from(components["java"])
                }
            }
            repositories {
                maven {
                    name = "nextCluster"
                    url = uri("https://nexus.nextcluster.net/repository/maven-snapshots/")
                    credentials {
                        username = project.property("REPOSITORY_USERNAME").toString()
                        password = project.property("REPOSITORY_PASSWORD").toString()
                    }
                }
            }
        }
    }

    tasks.test {
        useJUnitPlatform()
    }
}