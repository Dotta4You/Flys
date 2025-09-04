plugins {
    kotlin("jvm") version "2.2.10"
    id("com.gradleup.shadow") version "9.1.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "de.doetchen"
version = "0.2"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        minecraftVersion("1.21.8")
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("kotlin", "de.doetchen.projects.libs.kotlin")
}