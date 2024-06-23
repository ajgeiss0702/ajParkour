plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
    `maven-publish`
}

group = "us.ajg0702"
version = "3.0.0"

repositories {
  mavenCentral()

  maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
  maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
  maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
  maven { url = uri("https://maven.enginehub.org/repo/") }
  maven { url = uri("https://repo.ajg0702.us/releases") }

  mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.0.1")

    implementation("net.kyori:adventure-api:4.13.0")
    implementation("net.kyori:adventure-text-minimessage:4.13.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")

    implementation("us.ajg0702:ajUtils:1.2.24")
    implementation("us.ajg0702.commands.platforms.bukkit:bukkit:1.1.0")
    implementation("us.ajg0702.commands.api:api:1.1.0")

    implementation("com.github.cryptomorin:XSeries:9.4.0")

    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
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

    archiveClassifier.set("")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
        }
    }

    repositories {

        val mavenUrl = "https://repo.ajg0702.us/releases"

        if(!System.getenv("REPO_TOKEN").isNullOrEmpty()) {
            maven {
                url = uri(mavenUrl)
                name = "ajRepo"

                credentials {
                    username = "plugins"
                    password = System.getenv("REPO_TOKEN")
                }
            }
        }
    }
}

