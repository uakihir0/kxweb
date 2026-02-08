pluginManagement {
    includeBuild("plugins")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "kxweb"

include("core")

// Future modules:
// include("auth")
// include("stream")

plugins {
    // To obtain an appropriate JVM environment in CI environments, etc.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
