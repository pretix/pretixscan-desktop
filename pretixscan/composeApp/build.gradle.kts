import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("kotlin-kapt")
    alias(libs.plugins.osdetector)
    alias(libs.plugins.composeHotReload)
}

// Task to generate version file
val generateVersionFile = tasks.register("generateVersionFile") {
    val outputDir = layout.buildDirectory.dir("generated/source/version/commonMain/kotlin")
    val versionCode: String by project

    inputs.property("version", project.version)
    inputs.property("versionCode", versionCode)
    outputs.dir(outputDir)

    doLast {
        val versionFile = outputDir.get().asFile.resolve("eu/pretix/desktop/generated/AppVersion.kt")
        versionFile.parentFile.mkdirs()

        versionFile.writeText(
            """
            // Generated file - do not edit
            package eu.pretix.desktop.generated
            
            object AppVersion {
                const val VERSION_NAME: String = "${project.version}"
                const val VERSION_CODE: Int = ${versionCode}
            }
        """.trimIndent()
        )
    }
}

kotlin {
    jvm("desktop")

    sourceSets {
        // Add generated source to the source set
        val commonMain by getting {
            kotlin.srcDir(layout.buildDirectory.dir("generated/source/version/commonMain/kotlin"))
        }

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

            // unstyled components
            implementation(libs.composables.core)

            // additional default icons
            implementation(libs.jetbrains.material.icons)
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
            implementation(libs.coroutines.swing)

            implementation(libs.webcam.driver)
            implementation(libs.apache.pdfbox)


            compileOnly(libs.requery)
            compileOnly(libs.requery.processor)
            implementation(project(":libpretixsync"))
            implementation(project(":libpretixprint"))


            // phone number validation
            implementation(libs.google.libphone)

            // Gadulka links to JavaFX to provide playback of sounds on JVM/desktop
            val fxSuffix = when (osdetector.classifier) {
                "linux-x86_64" -> "linux"
                "linux-aarch_64" -> "linux-aarch64"
                "windows-x86_64" -> "win"
                "osx-x86_64" -> "mac"
                "osx-aarch_64" -> "mac-aarch64"
                else -> throw IllegalStateException("Unknown OS: ${osdetector.classifier}")
            }
            implementation("org.openjfx:javafx-base:19:${fxSuffix}")
            implementation("org.openjfx:javafx-graphics:19:${fxSuffix}")
            implementation("org.openjfx:javafx-controls:19:${fxSuffix}")
            implementation("org.openjfx:javafx-swing:19:${fxSuffix}")
            implementation("org.openjfx:javafx-web:19:${fxSuffix}")
            implementation("org.openjfx:javafx-media:19:${fxSuffix}")
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

// Make compilation depend on version generation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(generateVersionFile)
}

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

            macOS {
                iconFile.set(File("logo/pretix_app_icon.icns"))
            }
            windows {
                iconFile.set(File("logo/pretix_app_icon.ico"))

                // MSI specific configuration
                console = false
                dirChooser = true
                perUserInstall = false
                menuGroup = "pretixSCAN"
                shortcut = true

                // Upgrade settings - consistent UUID for upgrade support
                upgradeUuid = "550e8400-e29b-41d4-a716-446655440000"

            }
            linux {
                iconFile.set(File("logo/pretix_app_icon.png"))
            }
        }
    }
}