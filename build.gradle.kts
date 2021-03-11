plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
}

group = "us.ajg0702"
version = "2.10.11"

repositories {
  mavenCentral()

  maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
  maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
  maven { url = uri("http://repo.extendedclip.com/content/repositories/placeholderapi/") }
  maven { url = uri("http://maven.enginehub.org/repo/") }
  maven { url = uri("https://gitlab.com/api/v4/projects/19978391/packages/maven") }
}

dependencies {
  compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
  compileOnly("me.clip:placeholderapi:2.10.4")
  compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.0.1")
  compileOnly(files("libs/InfiniteJump.jar"))

  implementation("com.zaxxer:HikariCP:3.4.5")
  implementation("org.slf4j:slf4j-simple:1.6.4")
  implementation("us.ajg0702:ajUtils:1.0.0")
}

tasks.withType<ProcessResources> {
    include("**/*.yml")
    filter<org.apache.tools.ant.filters.ReplaceTokens>(
            "tokens" to mapOf(
                    "VERSION" to project.version.toString()
            )
    )
}

tasks.shadowJar {
    relocate("us.ajg0702.utils", "us.ajg0702.parkour.utils")
    relocate("com.zaxxer.hikari", "us.ajg0702.parkour.hikari")

    archiveFileName.set("${baseName}-${version}.${extension}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}