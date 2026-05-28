plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform)
}

kotlin {
    androidLibrary {
        namespace = "in.vedicpanchang.astronomy"
        compileSdk = 37
        minSdk = 21

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Uncomment to add iOS support in the future:
    // iosArm64()
    // iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidUnitTest {
            dependsOn(commonTest.get())
        }
    }
}
