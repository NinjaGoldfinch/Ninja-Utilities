import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom")
    kotlin("jvm")
    id("maven-publish")
}

version = "${property("mod_version")}+${stonecutter.current.version}"
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("ninja_utils") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

repositories {
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Hypixel Mod API (packet definitions)
    implementation("net.hypixel:mod-api:1.0.1")

    // ResourcefulConfig (version-specific artifact per MC version)
    modImplementation("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${property("resourceful_mc_version")}:${property("resourceful_version")}")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation(kotlin("test"))
}

// Wire test source set to see client source set (Fabric Loom splits main/client)
sourceSets {
    named("test") {
        compileClasspath += sourceSets["client"].output + sourceSets["client"].compileClasspath
        runtimeClasspath += sourceSets["client"].output + sourceSets["client"].runtimeClasspath
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", stonecutter.current.version)
    inputs.property("loader_version", project.property("loader_version")!!)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to stonecutter.current.version,
            "loader_version" to project.property("loader_version")!!,
            "kotlin_loader_version" to project.property("kotlin_loader_version")!!
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name") as String
            from(components["java"])
        }
    }

    repositories {
    }
}
