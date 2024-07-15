import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation)
            api(libs.koin)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":libpretixsync"))
            implementation(project(":libpretixprint"))
        }

        all {
            languageSettings.optIn("org.koin.core.annotation.KoinExperimentalAPI")
        }
    }
}

// Set in the root gradle.properties
val version: String by project

compose.desktop {
    application {
        mainClass = "eu.pretix.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "pretixSCAN"
            packageVersion = version
            vendor = "rami.io GmbH"
            copyright = "pretix.eu, Raphael Michel"
            licenseFile.set(project.rootProject.file("LICENSE"))
            // run `./gradlew suggestModules` to determine list of modules
            modules("java.instrument", "java.sql.rowset", "jdk.unsupported")
            // if distribution size is not important, we can also:
            // alternatively: includeAllModules = true
        }
    }
}
