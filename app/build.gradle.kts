@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.dsl.NdkOptions.DebugSymbolLevel
import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import de.jensklingenberg.ktorfit.gradle.KtorfitGradleConfiguration
import io.gitlab.arturbosch.detekt.Detekt
import java.time.Instant
import java.util.UUID

plugins {
    kotlin("android")
    id("com.android.application")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("de.jensklingenberg.ktorfit") version "1.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("com.github.triplet.play") version "3.8.1"
}

val versionRegex = Regex("""\d+\.\d{1,2}\.\d{1,2}""")

android {
    namespace = "at.irfc.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "at.irfc.app"
        minSdk = 26
        targetSdk = 34

        versionName = project.getVersionName()
        versionCode = project.getVersionBuild()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        val keyPassword: String? = project.findProperty("keyPassword")?.toString()
        val storePassword: String? = project.findProperty("storePassword")?.toString()
        val keyAlias: String? = project.findProperty("keyAlias")?.toString()
        val keyStoreFile: File = rootDir.resolve(".keys/app_sign.jks")

        // Only create signingConfig, when all needed configs are available
        if (
            keyPassword != null &&
            storePassword != null &&
            keyAlias != null &&
            keyStoreFile.exists()
        ) {
            create("release") {
                this.storeFile = keyStoreFile
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
                this.storePassword = storePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
            ndk.debugSymbolLevel = DebugSymbolLevel.FULL.name
            buildConfigField(
                type = "String",
                name = "apiBaseUrl",
                value = "\"https://backend.irfc.fh-joanneum.at/api/\""
            )
            buildConfigField(
                type = "String",
                name = "deviceIdPrefix",
                value = "\"android\""
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            buildConfigField(
                type = "String",
                name = "apiBaseUrl",
                value = "\"https://backend.irfc-test.fh-joanneum.at/api/\""
            )
            // Use a new prefix for each debug build to make testing easier
            buildConfigField(
                type = "String",
                name = "deviceIdPrefix",
                value = "\"android-debug-${UUID.randomUUID()}\""
            )

            if (project.findProperty("signDebug") == "true") {
                signingConfig = signingConfigs.getByName("release")
                versionNameSuffix = "-test"
                applicationIdSuffix = ".test"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val ktorfitVersion = "1.4.1"

configure<KtorfitGradleConfiguration> {
    version = ktorfitVersion
}

ksp {
    arg("compose-destinations.codeGenPackageName", "at.irfc.app.generated.navigation")
    arg("room.schemaLocation", projectDir.resolve("schemas").absolutePath)
}

detekt {
    config = files(rootDir.resolve("detekt.yml"))
    buildUponDefaultConfig = true
    basePath = rootDir.path
}

tasks.withType<Detekt>().configureEach {
    this.jvmTarget = "1.8"
    jdkHome.set(file(System.getProperty("java.home")))
}

play {
    defaultToAppBundles.set(true)
    serviceAccountCredentials.set(rootDir.resolve(".keys/service-account.json"))
    track.set("internal")
    releaseStatus.set(ReleaseStatus.COMPLETED)
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Room DB
    val roomVersion = "2.5.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Ktorfit HTTP client
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")

    val ktorVersion = "2.3.1"
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // Koin DI
    val koinVersion = "3.4.1"
    compileOnly("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-android:$koinVersion")
    // Version is not aligned to other koin modules
    implementation("io.insert-koin:koin-androidx-compose:3.4.5")

    // Coil async image loader and caching
    val coilVersion = "2.4.0"
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")

    // Compose Destinations Navigation
    val composeDestinationsVersion = "1.9.42-beta"
    implementation("io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion")

    // Compose UI
    val composeBom = platform("androidx.compose:compose-bom:2023.05.01")
    implementation(composeBom)
    debugImplementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Material 3 does not have swipe to refresh yet TODO remove once added to material 3
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")

    implementation("com.github.jeziellago:compose-markdown:0.3.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Test tooling
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

tasks.register("release") {
    doLast {
        val versionParameter: String? = project.findProperty("v")?.toString()?.also { version ->
            if (!versionRegex.matches(version)) {
                throw IllegalArgumentException(
                    "Version argument 'v' must match $versionRegex (e.g. 2.10.2). " +
                        "For usage see README.md"
                )
            }
        }

        val buildParameter: Int? = project.findProperty("b")?.toString()?.let { build ->
            build.toIntOrNull() ?: throw IllegalArgumentException(
                "Build number argument 'b' must be an integer." +
                    "For usage see README.md"
            )
        }

        println("Checking if working tree is clean")
        val workingTreeClean = runCommand(
            "git",
            "diff",
            "--shortstat",
            "--exit-code",
            ignoreExitCode = true
        )
        if (workingTreeClean.exitValue != 0) {
            throw IllegalStateException(
                "Git working tree is not clean. " +
                    "Commit (or stash) all your local changes before making a release."
            )
        }

        // Always load it to ensure that the needed properties are defined and have the correct format
        val oldVersion = project.getVersionName()
        val oldBuild = project.getVersionBuild()

        val newVersion = versionParameter ?: oldVersion
        val newBuild = buildParameter ?: (oldBuild + 1)
        val versionTag = "v$newVersion-b$newBuild"

        println("Working tree is clean, changing version to $versionTag.")
        val file = rootDir.resolve("gradle.properties")
        file.writeText(
            file.readText()
                .replace(
                    Regex("^(version_name=)$versionRegex$", RegexOption.MULTILINE),
                    "$1$newVersion"
                )
                .replace(
                    Regex("^(version_build=)\\d+$", RegexOption.MULTILINE),
                    "$1$newBuild"
                )
        )

        println("Updating version")
        runCommand("git", "add", file.absolutePath)

        println("Committing version")
        runCommand("git", "commit", "-m", "Update version to $versionTag")

        println("Creating git tag $versionTag")
        runCommand("git", "tag", versionTag)

        println("Push git tag $versionTag to origin")
        runCommand("git", "push", "origin", versionTag)
    }
}

tasks.register("releaseTest") {
    doLast {
        println("Checking if working tree is clean")
        val workingTreeClean = runCommand(
            "git",
            "diff",
            "--shortstat",
            "--exit-code",
            ignoreExitCode = true
        )
        if (workingTreeClean.exitValue != 0) {
            throw IllegalStateException(
                "Git working tree is not clean. " +
                    "Commit (or stash) all your local changes before making a release."
            )
        }
        println("Working tree is clean")

        val version = project.getVersionName()
        // Divide epochSeconds by 60 to get epochMinutes.
        // This value is used as buildNumber and buildNumbers are limited to Integer
        // (see Year 2038 problem: https://en.wikipedia.org/wiki/Year_2038_problem)
        val versionTag = "v$version-test-${Instant.now().epochSecond / 60}"

        println("Creating git tag $versionTag")
        runCommand("git", "tag", versionTag)

        println("Push git tag $versionTag to origin")
        runCommand("git", "push", "origin", versionTag)
    }
}

fun runCommand(vararg args: String, ignoreExitCode: Boolean = false): ExecResult = exec {
    isIgnoreExitValue = ignoreExitCode
    commandLine(*args)
}

fun Project.getVersionName() = this.properties["version_name"]?.toString()
    ?.takeIf { it.matches(versionRegex) }
    ?: throw IllegalArgumentException(
        "version_name must be set in gradle.properties and match $versionRegex (e.g. 2.10.1)."
    )

fun Project.getVersionBuild() = this.properties["version_build"]?.toString()
    ?.toIntOrNull()
    ?: throw IllegalArgumentException(
        "version_build must be set in gradle.properties and must be an integer."
    )
