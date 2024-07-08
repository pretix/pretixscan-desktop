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
}

include(":libpretixsync")
project(":libpretixsync").projectDir = file("libpretixsync-repo/libpretixsync")

include(":libpretixprint")
project(":libpretixprint").projectDir = file("libpretixprint-repo/libpretixprint")

include(":composeApp")
//include(":packaging")