import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile = rootProject.file("keys/key.properties")
val keystoreProperties = Properties()
val isReleaseBuild = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("release", ignoreCase = true) || taskName.contains("bundle", ignoreCase = true)
}

if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
} else if (isReleaseBuild) {
    throw GradleException("Missing signing config: ${keystorePropertiesFile.absolutePath}")
}

android {
    namespace = "in.vedicpanchang.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "in.vedicpanchang.app"
        minSdk = 21
        targetSdk = 36
        versionCode = (project.findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = (project.findProperty("versionName") as String?) ?: "2.0.1"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val storeFileValue = keystoreProperties.getProperty("storeFile")
                    ?: throw GradleException("storeFile missing in keys/key.properties")
                if (storeFileValue.isBlank()) {
                    throw GradleException("storeFile missing in keys/key.properties")
                }
                val resolvedStoreFile = if (File(storeFileValue).isAbsolute) {
                    File(storeFileValue)
                } else {
                    rootProject.file("keys/$storeFileValue")
                }
                if (isReleaseBuild && !resolvedStoreFile.exists()) {
                    throw GradleException("Keystore file not found: ${resolvedStoreFile.absolutePath}")
                }
                storeFile = resolvedStoreFile
                storePassword = keystoreProperties.getProperty("storePassword")
                    ?: throw GradleException("storePassword missing in keys/key.properties")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                    ?: throw GradleException("keyAlias missing in keys/key.properties")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                    ?: throw GradleException("keyPassword missing in keys/key.properties")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures { compose = true }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Astronomy KMP module
    implementation(project(":astronomy"))

    // Kotlin
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Location
    implementation(libs.play.services.location)

    // Fonts
    implementation(libs.androidx.ui.text.google.fonts)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Desugaring (java.time for minSdk < 26)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
