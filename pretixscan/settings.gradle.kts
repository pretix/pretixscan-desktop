rootProject.name = "pretixscan"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
    versionCatalogs {
        create("nfcCoreLibs") {
            from(files("libpretixnfc-repo/libpretixnfc/gradle/libs.versions.toml"))
        }
        create("syncLibs") {
            from(files("libpretixsync-repo/libpretixsync/gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}


include(":libpretixsync")
project(":libpretixsync").projectDir = file("libpretixsync-repo/libpretixsync")

include(":libpretixprint")
project(":libpretixprint").projectDir = file("libpretixprint-repo/libpretixprint")

include(":libpretixnfc")
project(":libpretixnfc").projectDir = file("libpretixnfc-repo/libpretixnfc")

include(":libpretixnfc-desktop")
project(":libpretixnfc-desktop").projectDir = file("libpretixnfc-repo/libpretixnfc-desktop")

include(":composeApp")