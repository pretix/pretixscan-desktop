plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.google.protobuf) apply false
    alias(libs.plugins.app.cash.sqldelight) apply false
    alias(libs.plugins.osdetector) apply false
}

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("net.ltgt.gradle:gradle-apt-plugin:0.5")
    }
}

/**
 * Prints value of property "version" to stdout.
 *
 * Usage: ./gradlew -q printVersion
 */
task("printVersion") {
    doLast {
        val version = project.findProperty("version").toString()
        println("pretixVersion=$version")
    }
}