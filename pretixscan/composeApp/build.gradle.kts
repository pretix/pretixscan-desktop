import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("kotlin-kapt")
}

kotlin {
    jvm("desktop")

    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.compose)
            implementation(libs.androidx.navigation)
            implementation(libs.coroutines.core)
            implementation(libs.org.json)
            implementation(libs.joda.time)
            implementation(libs.vanniktech.multiplatform.locale)

            // play short audio files
            implementation(libs.gadulka)

            // webcam
            implementation(libs.sarxos.webcam)
            api(libs.koin)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.sqlite)
            implementation(libs.appdirs)
            implementation(libs.sqldelight.jvm)
            implementation(libs.androidx.lifecycle.runtime.desktop)
            implementation(libs.coroutines.jvm)
            implementation(libs.coroutines.swing)

            implementation(libs.webcam.driver)
            implementation(libs.apache.pdfbox)

            compileOnly(libs.requery)
            compileOnly(libs.requery.processor)
            implementation(project(":libpretixsync"))
            implementation(project(":libpretixprint"))
        }

        val desktopTest by getting
        desktopTest.dependencies {
            implementation(compose.desktop.uiTestJUnit4)
            implementation(compose.desktop.currentOs)
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
            packageVersion = version.toString()
            vendor = "rami.io GmbH"
            copyright = "pretix.eu, Raphael Michel"
            licenseFile.set(project.rootProject.file("LICENSE"))
            // run `./gradlew suggestModules` to determine list of modules
            modules("java.instrument", "java.sql.rowset", "jdk.unsupported")
            // if distribution size is not important, we can also:
            // alternatively: includeAllModules = true

            macOS {
                iconFile.set(File("logo/pretix_app_icon.icns"))
            }
            windows {
                iconFile.set(File("logo/pretix_app_icon.ico"))
            }
            linux {
                iconFile.set(File("logo/pretix_app_icon.png"))
            }
        }
    }
}


// KMP doesn't support signing on Windows yet https://youtrack.jetbrains.com/issue/CMP-5285
// Workaround is to create a custom task to do it, inspired by https://gist.github.com/dlozanovic/386e14cbde3fdf5449fde075058becbb
val signWindowsExe = tasks.register("signWindowsMsi") {
    group = "signing"
    description = "Sign Windows executable"

    dependsOn("createDistributable")

    doLast {
        val exeDir = project.rootDir.resolve("composeApp/build/compose/binaries/main/msi")
        val targetFile = exeDir.resolve("pretixSCAN-$version.msi")

        if (targetFile.exists()) {
            project.exec {
                commandLine(
                    "chmod", "755",
                    targetFile.absolutePath
                )
                // Capture and print output/errors
                standardOutput = System.out
                errorOutput = System.err
            }


            project.exec {
                commandLine(
                    "signcode.exe",
                    "-spc", "authenticode.spc",
                    "-v", "authenticode.pvk",
                    "-a", "sha1",
                    "-$", "commercial",
                    "-n", "pretixdroid",
                    "-i", "https://pretix.eu/",
                    "-t", "http://timestamp.verisign.com/scripts/timstamp.dll",
                    "-tr", "10",
                    targetFile.absolutePath
                )

                // Capture and print output/errors
                standardOutput = System.out
                errorOutput = System.err
            }

            project.exec {
                commandLine(
                    "chmod", "555",
                    targetFile.absolutePath
                )
                // Capture and print output/errors
                standardOutput = System.out
                errorOutput = System.err
            }

            println("Signed: ${targetFile.name}")
        } else {
            throw GradleException("Warning: File not found - ${targetFile.absolutePath}")
        }
    }
}