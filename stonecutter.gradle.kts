plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") apply false
}

stonecutter active "1.21.10"

stonecutter parameters {
    // Version swap parameters — add as source code diverges between versions
}

stonecutter tasks {
    tasks.register("build") {
        group = "build"
        description = "Build all versions"
        dependsOn(named("build"))
    }
}
